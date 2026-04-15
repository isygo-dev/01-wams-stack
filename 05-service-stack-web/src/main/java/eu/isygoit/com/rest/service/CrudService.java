package eu.isygoit.com.rest.service;

import eu.isygoit.com.rest.controller.constants.CtrlConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.*;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.helper.FieldAccessorCache;
import eu.isygoit.model.*;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Abstract base class for CRUD api operations with tenant-aware functionality.
 * Provides methods for creating, reading, updating, and deleting entities with performance optimizations.
 *
 * @param <I> the type of the identifier (must be Serializable)
 * @param <T> the entity type (must implement IIdAssignable)
 * @param <R> the repository type (must extend JpaPagingAndSortingRepository)
 */
@Slf4j
public abstract class CrudService<I extends Serializable,
        T extends IIdAssignable<I>,
        R extends JpaPagingAndSortingRepository<T, I>>
        extends CrudServiceUtils<I, T, R>
        implements ICrudServiceOperations<I, T>, ICrudServiceEvents<I, T>, ICrudServiceUtils<I, T> {

    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];
    private final boolean isTenantAssignable = ITenantAssignable.class.isAssignableFrom(persistentClass);

    @Autowired
    private EntityManager entityManager;

    /**
     * Validates that an object is not null.
     *
     * @param object the object to validate
     * @throws BadArgumentException if the object is null
     */
    private static <I extends Serializable, T extends IIdAssignable<I>> void validateObjectNotNull(T object) {
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
    private static <I extends Serializable, T extends IIdAssignable<I>> void validateObjectIdNotNull(T object) {
        if (object.getId() == null) {
            log.error("Null ID provided for object: {}", object.getClass().getSimpleName());
            throw new NullIdentifierException(object.getClass().getSimpleName() + ": with id null");
        }
    }

    /**
     * Validates that the operation is not tenant-specific.
     *
     * @param operationName the name of the operation
     * @throws OperationNotAllowedException if the operation requires tenant-specific handling
     */
    private void validateNotTenantSpecific(String operationName) {
        if (isTenantAssignable) {
            log.error("{} operation on {} requires tenant-specific method", operationName, persistentClass.getSimpleName());
            throw new OperationNotAllowedException(operationName + persistentClass.getSimpleName() + " " + CtrlConstants.SHOULD_USE_SAAS_SPECIFIC_METHOD);
        }
    }

    /**
     * Counts all entities (non-tenant-specific).
     *
     * @return the total number of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Long count() {
        validateNotTenantSpecific("count ");
        log.info("Counting all {} entities", persistentClass.getSimpleName());
        var count = repository().count();
        log.debug("Count result: {}", count);
        return count;
    }

    /**
     * Checks if an entity exists by ID (non-tenant-specific).
     *
     * @param id the entity ID
     * @return true if the entity exists
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(I id) {
        validateNotTenantSpecific("existsById ");
        log.info("Checking existence of {} with ID: {}", persistentClass.getSimpleName(), id);
        var exists = repository().existsById(id);
        log.debug("Existence check result for ID {}: {}", id, exists);
        return exists;
    }

    /**
     * Creates a single entity (non-tenant-specific).
     *
     * @param object the entity to create
     * @return the created entity
     */
    @Override
    @Transactional
    public T create(T object) {
        try {
            validateNotTenantSpecific("create ");
            validateObjectNotNull(object);
            log.info("Creating {} entity", persistentClass.getSimpleName());
            log.debug("Input entity: {}", object);

            // Prepare entity
            assignCodeIfEmpty(object);
            var preparedObject = beforeCreate(object);
            log.debug("After pre-create hook: {}", preparedObject);

            // Save entity
            var savedObject = repository().saveAndFlush(preparedObject);
            log.debug("Saved entity: {}", savedObject);

            // Post-create processing
            var result = afterCreate(savedObject);
            log.info("Successfully created {} entity with ID: {}", persistentClass.getSimpleName(), result.getId());
            return result;
        } catch (DataIntegrityViolationException e) {
            throw new CreateConstraintsViolationException(e.getMessage());
        }
    }

    /**
     * Creates multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to create
     * @return the list of created entities
     */
    @Override
    @Transactional
    public List<T> createBatch(List<T> objects) {
        validateNotTenantSpecific("create ");
        validateListNotEmpty(objects);
        log.info("Creating {} {} entities", objects.size(), persistentClass.getSimpleName());

        // Process bulk creation in batch
        var result = repository().saveAll(objects.stream()
                .peek(obj -> log.debug("Preparing entity for creation: {}", obj))
                .map(this::assignCodeIfEmpty)
                .map(o -> beforeCreate((T) o))
                .toList());

        var finalResult = result.stream()
                .map(this::afterCreate)
                .toList();
        log.info("Successfully created {} {} entities", finalResult.size(), persistentClass.getSimpleName());
        return finalResult;
    }

    /**
     * Updates a single entity (non-tenant-specific).
     *
     * @param object the entity to update
     * @return the updated entity
     */
    @Override
    @Transactional
    public T update(T object) {
        try {
            validateNotTenantSpecific("update ");
            validateObjectNotNull(object);
            validateObjectIdNotNull(object);

            T original = repository().findById(object.getId())
                    .orElseThrow(() -> {
                        log.error("Entity {} with id {} not found during update",
                                persistentClass.getSimpleName(), object.getId());
                        return new ObjectNotFoundException("with id: " + object.getId());
                    });

            if (object instanceof IDirtyEntity) {
                validateObjectUpdatable(object, original);
            }

            log.info("Updating {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());

            // Preserve existing attributes and prepare update
            keepOriginalAttributes(object, original);
            assignCodeIfEmpty(object);
            var preparedObject = beforeUpdate(object);
            log.debug("After pre-update hook: {}", preparedObject);

            // Save updated entity
            var updatedObject = repository().saveAndFlush(preparedObject);
            var result = afterUpdate(updatedObject);
            log.info("Successfully updated {} entity with ID: {}", persistentClass.getSimpleName(), result.getId());
            return result;
        } catch (DataIntegrityViolationException e) {
            throw new UpdateConstraintsViolationException(e.getMessage());
        }
    }

    private void validateObjectExists(T object) {
        if (!repository().existsById(object.getId())) {
            throw new ObjectNotFoundException("with id: " + object.getId());
        }
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
    private void validateObjectUpdatable(T object, T original) throws ObjectNotModifiedException {
        IDirtyEntity dirtyEntity = (IDirtyEntity) object;
        Set<String> ignoredFields = dirtyEntity.ignoreFields();

        log.debug("Validating dirty state for {} entity with ID: {}",
                persistentClass.getSimpleName(), object.getId());

        // Walk every declared field in the class hierarchy and compare values
        if (!hasDirtyField(object, original, ignoredFields)) {
            log.warn("No dirty fields detected for {} entity with ID: {} — update skipped",
                    persistentClass.getSimpleName(), object.getId());
            throw new ObjectNotModifiedException(
                    persistentClass.getSimpleName() + " with id: " + object.getId());
        }

        log.debug("Dirty fields detected for {} entity with ID: {} — update allowed",
                persistentClass.getSimpleName(), object.getId());
    }

    private void validateObjectUpdatable(T object) throws ObjectNotModifiedException {
        // Fetch the persisted original — must exist for an update
        T original = repository().findById(object.getId())
                .orElseThrow(() -> {
                    log.error("Entity {} with id {} not found during dirty check",
                            persistentClass.getSimpleName(), object.getId());
                    return new ObjectNotFoundException("with id: " + object.getId());
                });

        validateObjectUpdatable(object, original);
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
    private boolean hasDirtyField(T incoming, T original, Set<String> ignoredFields) {
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
    private boolean objectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /**
     * Updates multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to update
     * @return the list of updated entities
     */
    @Override
    @Transactional
    public List<T> updateBatch(List<T> objects) {
        validateNotTenantSpecific("update ");
        validateListNotEmpty(objects);
        log.info("Updating {} {} entities", objects.size(), persistentClass.getSimpleName());

        // Process bulk update in batch
        var result = repository().saveAll(objects.stream()
                .peek(obj -> log.debug("Preparing entity for update: {}", obj))
                .map(this::keepOriginalAttributes)
                .map(this::assignCodeIfEmpty)
                .map(o -> beforeUpdate((T) o))
                .toList());

        var finalResult = result.stream()
                .map(this::afterUpdate)
                .toList();
        log.info("Successfully updated {} {} entities", finalResult.size(), persistentClass.getSimpleName());
        return finalResult;
    }

    /**
     * Deletes multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to delete
     */
    @Override
    @Transactional
    public void deleteBatch(List<T> objects) {
        validateNotTenantSpecific("delete ");
        validateListNotEmpty(objects);
        log.info("Deleting {} {} entities", objects.size(), persistentClass.getSimpleName());

        // Process deletion in batch
        beforeDelete(objects);
        if (CancelableEntity.class.isAssignableFrom(persistentClass)) {
            objects.forEach(this::handleEntityCancelation);
        } else {
            repository().deleteAllInBatch(objects);
        }
        afterDelete(objects);
        log.info("Successfully deleted {} {} entities", objects.size(), persistentClass.getSimpleName());
    }

    /**
     * Deletes a single entity by ID (non-tenant-specific).
     *
     * @param id the entity ID
     */
    @Override
    @Transactional
    public void delete(I id) {
        validateNotTenantSpecific("delete ");
        if (id == null) {
            log.error("Null ID provided for delete operation");
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        log.info("Deleting {} entity with ID: {}", persistentClass.getSimpleName(), id);

        // Process deletion
        repository().findById(id).ifPresentOrElse(object -> {
            beforeDelete(id);
            if (CancelableEntity.class.isAssignableFrom(persistentClass)) {
                handleEntityCancelation(object);
            } else {
                repository().delete(object);
            }
            afterDelete(id);
            log.info("Successfully deleted {} entity with ID: {}", persistentClass.getSimpleName(), id);
        }, () -> {
            log.error("Entity with ID: {} not found", id);
            throw new ObjectNotFoundException(" with id: " + id);
        });
    }

    /**
     * Retrieves all entities (non-tenant-specific).
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        validateNotTenantSpecific("findAll ");
        log.info("Retrieving all {} entities", persistentClass.getSimpleName());
        var list = repository().findAll();
        var result = CollectionUtils.isEmpty(list) ? List.<T>of() : afterFindAll(list);
        log.debug("Retrieved {} {} entities", result.size(), persistentClass.getSimpleName());
        return result;
    }

    /**
     * Retrieves paginated entities (non-tenant-specific).
     *
     * @param pageable the pagination parameters
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(Pageable pageable) {
        validateNotTenantSpecific("findAll ");
        log.info("Retrieving paginated {} entities", persistentClass.getSimpleName());
        log.debug("Pageable: {}", pageable);
        var content = repository().findAll(pageable).getContent();
        var result = content.isEmpty() ? List.<T>of() : afterFindAll(content);
        log.debug("Retrieved {} paginated {} entities", result.size(), persistentClass.getSimpleName());
        return result;
    }

    /**
     * Retrieves an entity by ID (non-tenant-specific).
     *
     * @param id the entity ID
     * @return an Optional containing the entity if found
     * @throws BadArgumentException if ID is null
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(I id) {
        validateNotTenantSpecific("findById ");
        if (id == null) {
            log.error("Null ID provided for findById operation");
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        log.info("Retrieving {} entity with ID: {}", persistentClass.getSimpleName(), id);
        var result = repository().findById(id)
                .map(this::afterFindById);
        log.debug("Find by ID result for ID {}: {}", id, result.isPresent() ? "found" : "not found");
        return result;
    }

    /**
     * Saves or updates an entity (non-tenant-specific).
     *
     * @param object the entity to save or update
     * @return the saved or updated entity
     */
    @Override
    @Transactional
    public T saveOrUpdate(T object) {
        validateNotTenantSpecific("saveOrUpdate ");
        validateObjectNotNull(object);
        log.info("Saving or updating {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        var result = object.getId() == null ? create(object) : update(object);
        log.info("Successfully saved or updated {} entity with ID: {}", persistentClass.getSimpleName(), result.getId());
        return result;
    }

    /**
     * Saves or updates multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to save or update
     * @return the list of saved or updated entities
     */
    @Override
    @Transactional
    public List<T> saveOrUpdate(List<T> objects) {
        validateNotTenantSpecific("saveOrUpdate ");
        validateListNotEmpty(objects);
        log.info("Saving or updating {} {} entities", objects.size(), persistentClass.getSimpleName());

        // Process save or update in batch
        var result = repository().saveAll(objects.stream()
                .peek(obj -> log.debug("Processing save or update for entity: {}", obj))
                .map(obj -> obj.getId() == null ? create(obj) : update(obj))
                .toList());
        log.info("Successfully saved or updated {} {} entities", result.size(), persistentClass.getSimpleName());
        return result;
    }

    /**
     * Retrieves entities filtered by criteria (non-tenant-specific).
     *
     * @param criteria the list of filter criteria
     * @return the list of filtered entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria) {
        validateNotTenantSpecific("findAllByCriteriaFilter ");
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Null or empty criteria provided for findAllByCriteriaFilter");
            throw new EmptyCriteriaFilterException("Criteria filter list is null or empty");
        }
        log.info("Retrieving {} entities by criteria", persistentClass.getSimpleName());
        log.debug("Criteria: {}", criteria);
        var specification = CriteriaHelper.buildSpecification(null, criteria, persistentClass);
        var result = repository().findAll((Sort) specification);
        log.debug("Retrieved {} filtered {} entities", result.size(), persistentClass.getSimpleName());
        return result;
    }

    /**
     * Retrieves paginated entities filtered by criteria (non-tenant-specific).
     *
     * @param criteria    the list of filter criteria
     * @param pageRequest the pagination parameters
     * @return the list of filtered entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest) {
        validateNotTenantSpecific("findAllByCriteriaFilter ");
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Null or empty criteria provided for findAllByCriteriaFilter");
            throw new EmptyCriteriaFilterException("Criteria filter list is null or empty");
        }
        log.info("Retrieving paginated {} entities by criteria", persistentClass.getSimpleName());
        log.debug("Criteria: {}, PageRequest: {}", criteria, pageRequest);
        Specification<T> specification = CriteriaHelper.buildSpecification(null, criteria, persistentClass);
        var result = repository().findAll(specification, pageRequest).getContent();
        log.debug("Retrieved {} filtered paginated {} entities", result, persistentClass.getSimpleName());
        return result;
    }

    @Override
    public List<T> getByIdIn(List<I> ids) {
        return repository().findByIdIn(ids);
    }

    /**
     * Hook called before creating an entity. Can be overridden by subclasses.
     *
     * @param object the entity to create
     * @return the processed entity
     */
    @Override
    public T beforeCreate(T object) {
        log.debug("Pre-create hook called for {} entity", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook called after creating an entity. Can be overridden by subclasses.
     *
     * @param object the created entity
     * @return the processed entity
     */
    @Override
    public T afterCreate(T object) {
        log.debug("Post-create hook called for {} entity", persistentClass.getSimpleName());
        return object;
    }

    /**
     * Hook called before deleting an entity. Can be overridden by subclasses.
     *
     * @param id the ID of the entity to delete
     */
    @Override
    public void beforeDelete(I id) {
        log.debug("Pre-delete hook called for {} entity with ID: {}", persistentClass.getSimpleName(), id);
    }

    /**
     * Hook called after deleting an entity. Can be overridden by subclasses.
     *
     * @param id the ID of the deleted entity
     */
    @Override
    public void afterDelete(I id) {
        log.debug("Post-delete hook called for {} entity with ID: {}", persistentClass.getSimpleName(), id);
    }

    /**
     * Hook called before deleting multiple entities. Can be overridden by subclasses.
     *
     * @param objects the list of entities to delete
     */
    @Override
    public void beforeDelete(List<T> objects) {
        log.debug("Pre-delete hook called for {} {} entities", objects.size(), persistentClass.getSimpleName());
    }

    /**
     * Hook called after deleting multiple entities. Can be overridden by subclasses.
     *
     * @param objects the list of deleted entities
     */
    @Override
    public void afterDelete(List<T> objects) {
        log.debug("Post-delete hook called for {} {} entities", objects.size(), persistentClass.getSimpleName());
    }

    /**
     * Hook called before updating an entity. Can be overridden by subclasses.
     *
     * @param object the entity to update
     * @return the processed entity
     */
    @Override
    public T beforeUpdate(T object) {
        log.debug("Pre-update hook called for {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        return object;
    }

    /**
     * Hook called after updating an entity. Can be overridden by subclasses.
     *
     * @param object the updated entity
     * @return the processed entity
     */
    @Override
    public T afterUpdate(T object) {
        log.debug("Post-update hook called for {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        return object;
    }

    /**
     * Hook called after retrieving all entities. Can be overridden by subclasses.
     *
     * @param list the list of retrieved entities
     * @return the processed list of entities
     */
    @Override
    public List<T> afterFindAll(List<T> list) {
        log.debug("Post-find-all hook called for {} {} entities", list.size(), persistentClass.getSimpleName());
        return list;
    }

    /**
     * Hook called after retrieving an entity by ID. Can be overridden by subclasses.
     *
     * @param object the retrieved entity
     * @return the processed entity
     */
    @Override
    public T afterFindById(T object) {
        log.debug("Post-find-by-id hook called for {} entity with ID: {}",
                persistentClass.getSimpleName(), object.getId());
        return object;
    }

    /**
     * Preserves original attributes for file and image entities.
     *
     * @param object   the entity to update
     * @param existing the existing entity
     * @return the entity with preserved attributes
     */
    private T keepOriginalAttributes(T object, T existing) {
        log.debug("Preserving attributes for {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        applyIfInstance(object, existing, IFileEntity.class, (t, s) -> {
            t.setType(s.getType());
            t.setFileName(s.getFileName());
            t.setOriginalFileName(s.getOriginalFileName());
            t.setPath(s.getPath());
            t.setExtension(s.getExtension());
        });
        applyIfInstance(object, existing, IImageEntity.class, (t, s) -> {
            t.setImagePath(s.getImagePath());
        });
        return object;
    }

    private T keepOriginalAttributes(T object) {
        repository().findById(object.getId()).ifPresent(existing -> {
            keepOriginalAttributes(object, existing);
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
    private <X> void applyIfInstance(T target, T source, Class<X> type, BiConsumer<X, X> action) {
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
    private void handleEntityCancelation(T object) {
        log.debug("Handling deletion for {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        if (object instanceof CancelableEntity cancelable && !cancelable.getCheckCancel()) {
            cancelable.setCheckCancel(true);
            cancelable.setCancelDate(Date.from(Instant.now()));
            repository().save(object);
        }
    }
}