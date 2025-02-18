package eu.isygoit.com.rest.service.impl.utils;

import eu.isygoit.annotation.SrvRepo;
import eu.isygoit.app.ApplicationContextService;
import eu.isygoit.com.rest.service.IAssignableCodeService;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.constants.DomainConstants;
import eu.isygoit.constants.LogConstants;
import eu.isygoit.exception.BadArgumentException;
import eu.isygoit.exception.JpaRepositoryNotDefinedException;
import eu.isygoit.exception.NextCodeServiceNotDefinedException;
import eu.isygoit.exception.OperationNotAllowedException;
import eu.isygoit.model.AssignableCode;
import eu.isygoit.model.AssignableDomain;
import eu.isygoit.model.AssignableId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The type Crud service utils.
 *
 * @param <I> the type parameter
 * @param <E> the type parameter
 * @param <R> the type parameter
 */
@Slf4j
public abstract class CrudServiceUtils<I extends Serializable, E extends AssignableId, R extends Repository<E, I>>
        implements ICrudServiceUtils<I, E> {

    @Getter
    private final Class<E> persistentClass = Optional.ofNullable(getClass().getGenericSuperclass())
            .filter(type -> type instanceof ParameterizedType)
            .map(type -> (ParameterizedType) type)
            .map(paramType -> paramType.getActualTypeArguments())
            .filter(args -> args.length > 1)
            .map(args -> args[1])
            .filter(Class.class::isInstance)
            .map(clazz -> (Class<E>) clazz)
            .orElseThrow(() -> new IllegalStateException("Could not determine persistent class"));

    @Getter
    @Autowired
    private ApplicationContextService contextService;

    private R repository;

    /**
     * Gets domain or default.
     *
     * @param <E>        the type parameter
     * @param fileEntity the file entity
     * @return the domain or default
     */
    public static <E extends AssignableId> String getDomainOrDefault(E fileEntity) {
        // Log the start of domain retrieval
        log.debug("Attempting to retrieve domain for entity: {}", fileEntity.getClass().getSimpleName());

        // Use Optional to handle domain retrieval from the entity
        return Optional.ofNullable(fileEntity)
                .filter(AssignableDomain.class::isInstance)
                .map(AssignableDomain.class::cast)
                .map(AssignableDomain::getDomain)
                .orElseGet(() -> {
                    // Log and return the default domain if no domain is found
                    log.warn("Domain not found for entity {}, returning default domain: {}", fileEntity.getClass().getSimpleName(), DomainConstants.DEFAULT_DOMAIN_NAME);
                    return DomainConstants.DEFAULT_DOMAIN_NAME;
                });
    }

    @Override
    public final R getRepository() throws JpaRepositoryNotDefinedException {
        // Log when starting the process of retrieving the repository
        log.debug("Attempting to retrieve repository for class: {}", this.getClass().getSimpleName());

        // Use Optional to handle repository retrieval in a more readable manner
        return Optional.ofNullable(repository)
                .orElseGet(() -> {
                    // Fetching the repository annotation from the current class
                    var controllerDefinition = this.getClass().getAnnotation(SrvRepo.class);

                    // Check if the annotation is missing
                    if (controllerDefinition == null) {
                        // Log and throw an error if the annotation is not found
                        log.error("<Error>: Repository bean not defined for {}", this.getClass().getSimpleName());
                        throw new JpaRepositoryNotDefinedException("JpaRepository not defined for " + this.getClass().getSimpleName());
                    }

                    // Log the annotation value (expected repository class)
                    log.debug("Found @SrvRepo annotation with value: {}", controllerDefinition.value().getSimpleName());

                    // Retrieve the repository bean from the context
                    return Optional.ofNullable(getContextService().getBean(controllerDefinition.value()))
                            .map(bean -> {
                                // Successfully retrieved the repository, assign it and log
                                repository = (R) bean;
                                log.debug("Repository bean {} successfully retrieved", controllerDefinition.value().getSimpleName());
                                return repository;
                            })
                            .orElseThrow(() -> {
                                // Log and throw an exception if the bean is not found
                                log.error("<Error>: Bean {} not found", controllerDefinition.value().getSimpleName());
                                return new JpaRepositoryNotDefinedException("JpaRepository " + controllerDefinition.value().getSimpleName() + " not found");
                            });
                });
    }

    @Override
    public <T> T logAndExecute(String logMessage, Supplier<T> action) {
        log.info(logMessage, persistentClass.getSimpleName());
        return action.get();
    }

    @Override
    public void validateObject(E object, boolean mustHaveId) {
        log.debug("Validating object: {}", object);
        if (Objects.isNull(object)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        if (mustHaveId && Objects.isNull(object.getId())) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }
        if (!mustHaveId && Objects.nonNull(object.getId())) {
            throw new BadArgumentException(LogConstants.NON_NULL_OBJECT_PROVIDED);
        }
    }

    @Override
    public void validateObjectId(I id) {
        log.debug("Validating object ID: {}", id);
        if (Objects.isNull(id)) {
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }
    }

    @Override
    public void checkOperationIsAllowedForDomain(String senderDomain, I id, E object) {
        log.debug("Checking domain access for sender: {} and entity with ID: {}", senderDomain, id);
        if (AssignableDomain.class.isAssignableFrom(getPersistentClass())
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {   // Is not Super domain
            if (!senderDomain.equals(((AssignableDomain) object).getDomain())) {     // And entity not belonging to the sender domain
                // =>Operation not allowed
                throw new OperationNotAllowedException("Delete forbidden for entity with ID: " + id);
            }
        }
    }

    @Override
    public <T extends AssignableId> T processCodeAssignable(T entity) {
        if (entity instanceof AssignableCode assignableCode) {
            if (!assignableCode.hasCode()
                    && this instanceof IAssignableCodeService assignableCodeService) {
                assignableCodeService.getNextCode()
                        .ifPresentOrElse(code -> assignableCode.setCode((String) code),
                                () -> new NextCodeServiceNotDefinedException("service call fail"));
            }
        }

        return entity;
    }

    @Override
    public <T extends AssignableId> T processDomainAssignable(String senderDomain, T entity) {
        if (entity instanceof AssignableDomain saasEntity
                && !DomainConstants.SUPER_DOMAIN_NAME.equals(senderDomain)) {
            //force to sender domain except if the sender is the super domain
            saasEntity.setDomain(senderDomain != null ? senderDomain : DomainConstants.DEFAULT_DOMAIN_NAME);
        }
        return entity;
    }
}