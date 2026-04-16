package eu.isygoit.com.rest.service;

import eu.isygoit.annotation.InjectRepository;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.*;
import eu.isygoit.helper.FieldAccessorCache;
import eu.isygoit.model.*;
import eu.isygoit.model.jakarta.CancelableEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The type Crud api utils.
 *
 * @param <I> the type parameter
 * @param <T> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudServiceUtils<I extends Serializable, T extends IIdAssignable<I>, R extends Repository>
        implements ICrudServiceUtils<I, T> {

    @Autowired
    private ApplicationContextService applicationContextService;

    private R repository;

    /**
     * Validates that an object is not null.
     *
     * @param object the object to validate
     * @throws BadArgumentException if the object is null
     */
    protected static <I extends Serializable, T extends IIdAssignable<I>> void validateObjectNotNull(T object) {
        if (object == null) {
            log.error("Null object provided for operation");
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
    }

    /**
     * Validates that an object's ID is not null.
     *
     * @param object the object to validate
     * @throws NullIdentifierException if the ID is null
     */
    protected static <I extends Serializable, T extends IIdAssignable<I>> void validateObjectIdNotNull(T object) {
        if (object.getId() == null) {
            log.error("Null ID provided for object: {}", object.getClass().getSimpleName());
            throw new NullIdentifierException(object.getClass().getSimpleName() + ": with id null");
        }
    }

    /**
     * Validates that a list is not empty.
     *
     * @param objects the list to validate
     * @throws EmptyListException if the list is empty
     */
    protected static void validateListNotEmpty(List objects) {
        if (CollectionUtils.isEmpty(objects)) {
            log.error("Empty or null list provided for operation");
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
    }

    @Override
    public final R repository() throws JpaRepositoryNotDefinedException {
        if (this.repository == null) {
            InjectRepository controllerDefinition = this.getClass().getAnnotation(InjectRepository.class);
            if (controllerDefinition != null) {
                this.repository = (R) applicationContextService.getBean(controllerDefinition.value())
                        .orElseThrow(() -> new JpaRepositoryNotDefinedException("JpaRepository " + controllerDefinition.value().getSimpleName() + " not found"));
            } else {
                log.error("<Error>: Repository bean not defined for {}", this.getClass().getSimpleName());
                throw new JpaRepositoryNotDefinedException("JpaRepository");
            }
        }

        return this.repository;
    }

    /**
     * Assign code if empty.
     *
     * @param object the object
     * @return the object
     */
    @SuppressWarnings("unchecked")
    public Object assignCodeIfEmpty(Object object) {
        if (this instanceof ICodeAssignableService codeAssignableService &&
                object instanceof ICodeAssignable codeAssignable &&
                !StringUtils.hasText(codeAssignable.getCode())) {
            codeAssignable.setCode(codeAssignableService.getNextCode());
        }
        return object;
    }

    /**
     * Validates that the entity has at least one dirty (changed) field before allowing an update.
     * <p>
     * Performs a field-by-field comparison against the incoming object.
     * Fields listed in {@link IDirtyEntity#ignoreFields()}
     * are skipped entirely. If no meaningful difference is detected, the update is rejected.
     *
     * @param object   the incoming entity to be updated; must implement {@link IDirtyEntity}
     * @param original the persisted original entity
     * @throws ObjectNotModifiedException if the entity carries no dirty (changed) fields
     */
    protected void validateObjectUpdatable(T object, T original) throws ObjectNotModifiedException {
        if (!(object instanceof IDirtyEntity dirtyEntity)) {
            return;
        }

        Set<String> ignoredFields = dirtyEntity.ignoreFields();

        log.debug("Validating dirty state for {} entity with ID: {}",
                object.getClass().getSimpleName(), object.getId());

        // Walk every declared field in the class hierarchy and compare values
        if (!hasDirtyField(object, original, ignoredFields)) {
            log.warn("No dirty fields detected for {} entity with ID: {} — update skipped",
                    object.getClass().getSimpleName(), object.getId());
            throw new ObjectNotModifiedException(
                    object.getClass().getSimpleName() + " with id: " + object.getId());
        }

        log.debug("Dirty fields detected for {} entity with ID: {} — update allowed",
                object.getClass().getSimpleName(), object.getId());
    }

    /**
     * Performs a comparison between the incoming and the original entity using cached field accessors,
     * walking up the entire class hierarchy. Fields whose names appear in {@code ignoredFields}
     * are skipped.
     *
     * @param incoming      the entity carrying the requested changes
     * @param original      the entity as currently persisted
     * @param ignoredFields field names that must not be considered when determining dirtiness
     * @return {@code true} if at least one tracked field differs between the two entities
     */
    protected boolean hasDirtyField(T incoming, T original, Set<String> ignoredFields) {
        for (FieldAccessorCache.FieldAccessor accessor : FieldAccessorCache.getAccessors(incoming.getClass())) {
            if (ignoredFields != null && ignoredFields.contains(accessor.name())) {
                log.trace("Skipping ignored field '{}' during dirty check", accessor.name());
                continue;
            }

            Object incomingValue = accessor.get(incoming);
            Object originalValue = accessor.get(original);

            if (!objectsEqual(incomingValue, originalValue)) {
                log.debug("Dirty field detected: '{}' changed from [{}] to [{}]",
                        accessor.name(), originalValue, incomingValue);
                return true;
            }
        }

        return false;
    }

    /**
     * Null-safe equality check used during dirty-field comparison.
     *
     * @param a first value
     * @param b second value
     * @return {@code true} if both values are considered equal
     */
    protected boolean objectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Preserves original attributes for file and image entities.
     *
     * @param object   the entity to update
     * @param existing the existing entity
     * @return the entity with preserved attributes
     */
    protected T keepOriginalAttributes(T object, T existing) {
        log.debug("Preserving attributes for {} entity with ID: {}", object.getClass().getSimpleName(), object.getId());
        applyIfInstance(object, existing, IFileEntity.class, (t, s) -> {
            if (StringUtils.hasText(s.getPath())) {
                t.setType(s.getType());
                t.setFileName(s.getFileName());
                t.setOriginalFileName(s.getOriginalFileName());
                t.setPath(s.getPath());
                t.setExtension(s.getExtension());
            }
        });
        applyIfInstance(object, existing, IImageEntity.class, (t, s) -> {
            if (StringUtils.hasText(s.getImagePath())) {
                t.setImagePath(s.getImagePath());
            }
        });
        return object;
    }

    /**
     * Applies attribute copying for specific entity types.
     *
     * @param target the target entity
     * @param source the source entity
     * @param type   the entity type
     * @param action the attribute copying action
     */
    protected <X> void applyIfInstance(T target, T source, Class<X> type, BiConsumer<X, X> action) {
        if (type.isInstance(target) && type.isInstance(source)) {
            log.debug("Applying {} attributes for entity", type.getSimpleName());
            action.accept(type.cast(target), type.cast(source));
        }
    }

    /**
     * Handles entity deletion, supporting soft deletion for CancelableEntity.
     *
     * @param object the entity to delete
     */
    protected void handleEntityCancelation(T object) {
        log.debug("Handling deletion for {} entity with ID: {}", object.getClass().getSimpleName(), object.getId());
        if (object instanceof CancelableEntity cancelable && !cancelable.getCheckCancel()) {
            cancelable.setCheckCancel(true);
            cancelable.setCancelDate(LocalDateTime.now());
        }
    }
}
