package eu.isygoit.com.rest.service;

import eu.isygoit.constants.LogConstants;
import eu.isygoit.constants.TenantConstants;
import eu.isygoit.exception.*;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IFileEntity;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.IImageEntity;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.jakarta.CancelableEntity;
import eu.isygoit.repository.JpaPagingAndSortingRepository;
import eu.isygoit.repository.JpaPagingAndSortingTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
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
import java.util.function.BiConsumer;

/**
 * Abstract base class for CRUD service operations with tenant-aware functionality.
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
        implements ICrudServiceMethods<I, T>, ICrudTenantServiceMethods<I, T>, ICrudServiceEvents<I, T>, ICrudServiceUtils<I, T> {

    private static final String SHOULD_USE_SAAS_SPECIFIC_METHOD = "should use SAAS-specific method";
    private final Class<T> persistentClass = (Class<T>) ((ParameterizedType) getClass()
            .getGenericSuperclass()).getActualTypeArguments()[1];
    private final boolean isTenantAssignable = ITenantAssignable.class.isAssignableFrom(persistentClass);

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
     * Validates that a list is not empty.
     *
     * @param objects the list to validate
     * @throws EmptyListException if the list is empty
     */
    private static <I extends Serializable, T extends IIdAssignable<I>> void validateListNotEmpty(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            log.error("Empty or null list provided for operation");
            throw new EmptyListException(LogConstants.EMPTY_OBJECT_LIST_PROVIDED);
        }
    }

    /**
     * Retrieves the tenant-aware repository, throwing an exception if not applicable.
     *
     * @return the tenant-aware repository
     * @throws OperationNotSupportedException if the entity or repository is not tenant-aware
     */
    private JpaPagingAndSortingTenantAssignableRepository getTenantAssignableRepository() {
        if (!isTenantAssignable || !(repository() instanceof JpaPagingAndSortingTenantAssignableRepository jpaRepo)) {
            log.error("Entity {} is not tenant assignable", persistentClass.getSimpleName());
            throw new OperationNotSupportedException("Entity is not tenant assignable: " + persistentClass.getSimpleName());
        }
        return (JpaPagingAndSortingTenantAssignableRepository) repository();
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
            throw new OperationNotAllowedException(operationName + persistentClass.getSimpleName() + " " + SHOULD_USE_SAAS_SPECIFIC_METHOD);
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
     * Counts entities for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @return the number of entities for the tenant
     */
    @Override
    @Transactional(readOnly = true)
    public Long count(String tenant) {
        var jpaRepo = getTenantAssignableRepository();
        log.info("Counting {} entities for tenant: {}", persistentClass.getSimpleName(), tenant);
        var count = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.count()
                : jpaRepo.countByTenantIgnoreCase(tenant);

        log.debug("Count result for tenant {}: {}", tenant, count);
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
     * Checks if an entity exists by ID for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param id     the entity ID
     * @return true if the entity exists for the tenant
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String tenant, I id) {
        var jpaRepo = getTenantAssignableRepository();
        log.info("Checking existence of {} with ID: {} for tenant: {}", persistentClass.getSimpleName(), id, tenant);
        var exists = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.existsById(id)
                : jpaRepo.existsByIdAndTenantIgnoreCase(id, tenant);

        log.debug("Existence check result for ID {} and tenant {}: {}", id, tenant, exists);
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
        validateNotTenantSpecific("create ");
        validateObjectNotNull(object);
        log.info("Creating {} entity", persistentClass.getSimpleName());
        log.debug("Input entity: {}", object);

        // Prepare entity
        assignCodeIfEmpty(object);
        var preparedObject = beforeCreate(object);
        log.debug("After pre-create hook: {}", preparedObject);

        // Save entity
        var savedObject = repository().save(preparedObject);
        log.debug("Saved entity: {}", savedObject);

        // Post-create processing
        var result = afterCreate(savedObject);
        log.info("Successfully created {} entity with ID: {}", persistentClass.getSimpleName(), result.getId());
        return result;
    }

    /**
     * Creates a single entity for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param object the entity to create
     * @return the created entity
     */
    @Override
    @Transactional
    public T create(String tenant, T object) {
        var jpaRepo = getTenantAssignableRepository();
        validateObjectNotNull(object);
        log.info("Creating {} entity for tenant: {}", persistentClass.getSimpleName(), tenant);
        log.debug("Input entity: {}", object);

        // Set tenant and prepare entity
        ((ITenantAssignable) object).setTenant(tenant);
        assignCodeIfEmpty(object);
        var preparedObject = beforeCreate(object);
        log.debug("After pre-create hook: {}", preparedObject);

        // Save entity
        var savedObject = jpaRepo.save(preparedObject);
        log.debug("Saved entity: {}", savedObject);

        // Post-create processing
        var result = afterCreate((T) savedObject);
        log.info("Successfully created {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), result.getId(), tenant);
        return result;
    }

    /**
     * Creates multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to create
     * @return the list of created entities
     */
    @Override
    @Transactional
    public List<T> create(List<T> objects) {
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
     * Creates multiple entities in bulk for a specific tenant.
     *
     * @param tenant  the tenant identifier
     * @param objects the list of entities to create
     * @return the list of created entities
     */
    @Override
    @Transactional
    public List<T> create(String tenant, List<T> objects) {
        var jpaRepo = getTenantAssignableRepository();
        validateListNotEmpty(objects);
        log.info("Creating {} {} entities for tenant: {}", objects.size(), persistentClass.getSimpleName(), tenant);

        // Process bulk creation in batch
        var result = jpaRepo.saveAll(objects.stream()
                .peek(obj -> {
                    log.debug("Preparing entity for creation: {}", obj);
                    ((ITenantAssignable) obj).setTenant(tenant);
                })
                .map(this::assignCodeIfEmpty)
                .map(o -> beforeCreate((T) o))
                .toList());

        var finalResult = result.stream()
                .map(o -> afterCreate((T) o))
                .toList();
        log.info("Successfully created {} {} entities for tenant: {}",
                finalResult.size(), persistentClass.getSimpleName(), tenant);
        return finalResult;
    }

    /**
     * Creates and flushes a single entity (non-tenant-specific).
     *
     * @param object the entity to create
     * @return the created entity
     */
    @Override
    @Transactional
    public T createAndFlush(T object) {
        validateNotTenantSpecific("createAndFlush ");
        validateObjectNotNull(object);
        log.info("Creating and flushing {} entity", persistentClass.getSimpleName());
        log.debug("Input entity: {}", object);

        // Prepare and save entity
        assignCodeIfEmpty(object);
        var preparedObject = beforeCreate(object);
        var savedObject = repository().saveAndFlush(preparedObject);
        var result = afterCreate(savedObject);
        log.info("Successfully created and flushed {} entity with ID: {}",
                persistentClass.getSimpleName(), result.getId());
        return result;
    }

    /**
     * Creates and flushes a single entity for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param object the entity to create
     * @return the created entity
     */
    @Override
    @Transactional
    public T createAndFlush(String tenant, T object) {
        var jpaRepo = getTenantAssignableRepository();
        validateObjectNotNull(object);
        log.info("Creating and flushing {} entity for tenant: {}", persistentClass.getSimpleName(), tenant);
        log.debug("Input entity: {}", object);

        // Prepare and save entity
        ((ITenantAssignable) object).setTenant(tenant);
        assignCodeIfEmpty(object);
        var preparedObject = beforeCreate(object);
        var savedObject = jpaRepo.saveAndFlush(preparedObject);
        var result = afterCreate((T) savedObject);
        log.info("Successfully created and flushed {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), result.getId(), tenant);
        return result;
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
        validateNotTenantSpecific("update ");
        validateObjectNotNull(object);
        validateObjectIdNotNull(object);
        log.info("Updating {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());

        // Preserve existing attributes and prepare update
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        var preparedObject = beforeUpdate(object);
        log.debug("After pre-update hook: {}", preparedObject);

        // Save updated entity
        var updatedObject = repository().save(preparedObject);
        var result = afterUpdate(updatedObject);
        log.info("Successfully updated {} entity with ID: {}", persistentClass.getSimpleName(), result.getId());
        return result;
    }

    /**
     * Updates a single entity for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param object the entity to update
     * @return the updated entity
     */
    @Override
    @Transactional
    public T update(String tenant, T object) {
        var jpaRepo = getTenantAssignableRepository();
        validateObjectNotNull(object);
        validateObjectIdNotNull(object);
        log.info("Updating {} entity with ID: {} for tenant: {}", persistentClass.getSimpleName(), object.getId(), tenant);

        // Validate tenant access
        validateTenantAccess(tenant, object.getId());

        // Preserve attributes and prepare update
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        var preparedObject = beforeUpdate(object);
        log.debug("After pre-update hook: {}", preparedObject);

        // Save updated entity
        var updatedObject = jpaRepo.save(preparedObject);
        var result = afterUpdate((T) updatedObject);
        log.info("Successfully updated {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), result.getId(), tenant);
        return result;
    }

    /**
     * Updates multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to update
     * @return the list of updated entities
     */
    @Override
    @Transactional
    public List<T> update(List<T> objects) {
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
     * Updates multiple entities in bulk for a specific tenant.
     *
     * @param tenant  the tenant identifier
     * @param objects the list of entities to update
     * @return the list of updated entities
     */
    @Override
    @Transactional
    public List<T> update(String tenant, List<T> objects) {
        var jpaRepo = getTenantAssignableRepository();
        validateListNotEmpty(objects);
        log.info("Updating {} {} entities for tenant: {}", objects.size(), persistentClass.getSimpleName(), tenant);

        // Process bulk update in batch
        var result = jpaRepo.saveAll(objects.stream()
                .peek(obj -> {
                    log.debug("Preparing entity for update: {}", obj);
                    validateTenantAccess(tenant, obj.getId());
                })
                .map(this::keepOriginalAttributes)
                .map(this::assignCodeIfEmpty)
                .map(o -> beforeUpdate((T) o))
                .toList());

        var finalResult = result.stream()
                .map(o -> afterUpdate((T) o))
                .toList();
        log.info("Successfully updated {} {} entities for tenant: {}",
                finalResult.size(), persistentClass.getSimpleName(), tenant);
        return finalResult;
    }

    /**
     * Updates and flushes a single entity (non-tenant-specific).
     *
     * @param object the entity to update
     * @return the updated entity
     */
    @Override
    @Transactional
    public T updateAndFlush(T object) {
        validateNotTenantSpecific("updateAndFlush ");
        validateObjectNotNull(object);
        validateObjectIdNotNull(object);
        log.info("Updating and flushing {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());

        // Prepare and save entity
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        var preparedObject = beforeUpdate(object);
        var updatedObject = repository().saveAndFlush(preparedObject);
        var result = afterUpdate(updatedObject);
        log.info("Successfully updated and flushed {} entity with ID: {}",
                persistentClass.getSimpleName(), result.getId());
        return result;
    }

    /**
     * Updates and flushes a single entity for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param object the entity to update
     * @return the updated entity
     */
    @Override
    @Transactional
    public T updateAndFlush(String tenant, T object) {
        var jpaRepo = getTenantAssignableRepository();
        validateObjectNotNull(object);
        validateObjectIdNotNull(object);
        log.info("Updating and flushing {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), object.getId(), tenant);

        // Validate tenant access and entity existence
        if (!jpaRepo.existsById(object.getId())) {
            log.error("Entity with ID: {} not found for tenant: {}", object.getId(), tenant);
            throw new ObjectNotFoundException(" with id: " + object.getId() + " for tenant: " + tenant);
        }
        validateTenantAccess(tenant, object.getId());

        // Prepare and save entity
        keepOriginalAttributes(object);
        assignCodeIfEmpty(object);
        var preparedObject = beforeUpdate(object);
        var updatedObject = jpaRepo.saveAndFlush(preparedObject);
        var result = afterUpdate((T) updatedObject);
        log.info("Successfully updated and flushed {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), result.getId(), tenant);
        return result;
    }

    /**
     * Deletes a single entity by ID for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param id     the entity ID
     */
    @Override
    @Transactional
    public void delete(String tenant, I id) {
        var jpaRepo = getTenantAssignableRepository();
        if (id == null) {
            log.error("Null ID provided for delete operation");
            throw new BadArgumentException(LogConstants.NULL_OBJECT_ID_PROVIDED);
        }
        log.info("Deleting {} entity with ID: {} for tenant: {}", persistentClass.getSimpleName(), id, tenant);

        // Process deletion
        validateTenantAccess(tenant, id).ifPresentOrElse(object -> {
            beforeDelete(id);
            handleEntityDeletion((T) object);
            afterDelete(id);
            log.info("Successfully deleted {} entity with ID: {} for tenant: {}",
                    persistentClass.getSimpleName(), id, tenant);
        }, () -> {
            log.error("Entity with ID: {} not found for tenant: {}", id, tenant);
            throw new ObjectNotFoundException(" with id: " + id);
        });
    }

    /**
     * Deletes multiple entities in bulk for a specific tenant.
     *
     * @param tenant  the tenant identifier
     * @param objects the list of entities to delete
     */
    @Override
    @Transactional
    public void delete(String tenant, List<T> objects) {
        var jpaRepo = getTenantAssignableRepository();
        validateListNotEmpty(objects);
        log.info("Deleting {} {} entities for tenant: {}", objects.size(), persistentClass.getSimpleName(), tenant);

        // Validate tenant access and process deletion in batch
        beforeDelete(objects);
        objects.forEach(obj -> validateTenantAccess(tenant, obj.getId()));
        objects.forEach(this::handleEntityDeletion);
        jpaRepo.deleteAllInBatch(objects);
        afterDelete(objects);
        log.info("Successfully deleted {} {} entities for tenant: {}",
                objects.size(), persistentClass.getSimpleName(), tenant);
    }

    /**
     * Deletes multiple entities in bulk (non-tenant-specific).
     *
     * @param objects the list of entities to delete
     */
    @Override
    @Transactional
    public void delete(List<T> objects) {
        validateNotTenantSpecific("delete ");
        validateListNotEmpty(objects);
        log.info("Deleting {} {} entities", objects.size(), persistentClass.getSimpleName());

        // Process deletion in batch
        beforeDelete(objects);
        objects.forEach(this::handleEntityDeletion);
        repository().deleteAllInBatch(objects);
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
            handleEntityDeletion(object);
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
     * Retrieves all entities for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(String tenant) {
        var jpaRepo = getTenantAssignableRepository();
        log.info("Retrieving all {} entities for tenant: {}", persistentClass.getSimpleName(), tenant);
        var list = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.findAll()
                : jpaRepo.findByTenantIgnoreCase(tenant);
        var result = CollectionUtils.isEmpty(list) ? List.<T>of() : afterFindAll(list);
        log.debug("Retrieved {} {} entities for tenant: {}", result.size(), persistentClass.getSimpleName(), tenant);
        return result;
    }

    /**
     * Retrieves paginated entities for a specific tenant.
     *
     * @param tenant   the tenant identifier
     * @param pageable the pagination parameters
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAll(String tenant, Pageable pageable) {
        var jpaRepo = getTenantAssignableRepository();
        log.info("Retrieving paginated {} entities for tenant: {}", persistentClass.getSimpleName(), tenant);
        log.debug("Pageable: {}", pageable);
        var page = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.findAll(pageable)
                : jpaRepo.findByTenantIgnoreCase(tenant, pageable);

        var result = page.isEmpty() ? List.<T>of() : afterFindAll(page.getContent());
        log.debug("Retrieved {} paginated {} entities for tenant: {}",
                result.size(), persistentClass.getSimpleName(), tenant);
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
     * Retrieves an entity by ID for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param id     the entity ID
     * @return an Optional containing the entity if found
     * @throws BadArgumentException if ID is null
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(String tenant, I id) {
        var jpaRepo = getTenantAssignableRepository();
        if (id == null) {
            log.error("Null ID provided for findById operation");
            throw new BadArgumentException(LogConstants.NULL_OBJECT_PROVIDED);
        }
        log.info("Retrieving {} entity with ID: {} for tenant: {}", persistentClass.getSimpleName(), id, tenant);
        var result = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.findById(id)
                .map(o -> afterFindById((T) o))
                : jpaRepo.findById(id)
                .filter(t -> ((ITenantAssignable) t).getTenant().equals(tenant))
                .map(o -> afterFindById((T) o));

        log.debug("Find by ID result for ID {} and tenant {}: {}", id, tenant, result.isPresent() ? "found" : "not found");
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
     * Saves or updates an entity for a specific tenant.
     *
     * @param tenant the tenant identifier
     * @param object the entity to save or update
     * @return the saved or updated entity
     */
    @Override
    @Transactional
    public T saveOrUpdate(String tenant, T object) {
        var jpaRepo = getTenantAssignableRepository();
        validateObjectNotNull(object);
        log.info("Saving or updating {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), object.getId(), tenant);
        ((ITenantAssignable) object).setTenant(tenant);
        var result = object.getId() == null ? create(tenant, object) : update(tenant, object);
        log.info("Successfully saved or updated {} entity with ID: {} for tenant: {}",
                persistentClass.getSimpleName(), result.getId(), tenant);
        return result;
    }

    /**
     * Saves or updates multiple entities in bulk for a specific tenant.
     *
     * @param tenant  the tenant identifier
     * @param objects the list of entities to save or update
     * @return the list of saved or updated entities
     */
    @Override
    @Transactional
    public List<T> saveOrUpdate(String tenant, List<T> objects) {
        var jpaRepo = getTenantAssignableRepository();
        validateListNotEmpty(objects);
        log.info("Saving or updating {} {} entities for tenant: {}", objects.size(), persistentClass.getSimpleName(), tenant);

        // Process save or update in batch
        var result = jpaRepo.saveAll(objects.stream()
                .peek(obj -> {
                    log.debug("Processing save or update for entity: {}", obj);
                    ((ITenantAssignable) obj).setTenant(tenant);
                })
                .map(obj -> obj.getId() == null ? create(tenant, obj) : update(tenant, obj))
                .toList());
        log.info("Successfully saved or updated {} {} entities for tenant: {}",
                result.size(), persistentClass.getSimpleName(), tenant);
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

    /**
     * Retrieves entities filtered by criteria for a specific tenant.
     *
     * @param tenant   the tenant identifier
     * @param criteria the list of filter criteria
     * @return the list of filtered entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria) {
        var jpaRepo = getTenantAssignableRepository();
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Null or empty criteria provided for findAllByCriteriaFilter");
            throw new EmptyCriteriaFilterException("Criteria filter list is null or empty");
        }
        log.info("Retrieving {} entities by criteria for tenant: {}", persistentClass.getSimpleName(), tenant);
        log.debug("Criteria: {}", criteria);
        var specification = CriteriaHelper.buildSpecification(tenant, criteria, persistentClass);
        var result = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.findAll(specification)
                : jpaRepo.findAll(specification);

        log.debug("Retrieved {} filtered {} entities for tenant: {}", result.size(), persistentClass.getSimpleName(), tenant);
        return result;
    }

    /**
     * Retrieves paginated entities filtered by criteria for a specific tenant.
     *
     * @param tenant      the tenant identifier
     * @param criteria    the list of filter criteria
     * @param pageRequest the pagination parameters
     * @return the list of filtered entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest) {
        var jpaRepo = getTenantAssignableRepository();
        if (CollectionUtils.isEmpty(criteria)) {
            log.error("Null or empty criteria provided for findAllByCriteriaFilter");
            throw new EmptyCriteriaFilterException("Criteria filter list is null or empty");
        }
        log.info("Retrieving paginated {} entities by criteria for tenant: {}", persistentClass.getSimpleName(), tenant);
        log.debug("Criteria: {}, PageRequest: {}", criteria, pageRequest);
        var specification = CriteriaHelper.buildSpecification(tenant, criteria, persistentClass);
        var result = TenantConstants.SUPER_TENANT_NAME.equals(tenant)
                ? jpaRepo.findAll(specification, pageRequest).getContent()
                : jpaRepo.findAll(specification, pageRequest).getContent();

        log.debug("Retrieved {} filtered paginated {} entities for tenant: {}",
                result.size(), persistentClass.getSimpleName(), tenant);
        return result;
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
     * @param object the entity to update
     * @return the entity with preserved attributes
     */
    private T keepOriginalAttributes(T object) {
        repository().findById(object.getId()).ifPresent(existing -> {
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
     * Validates tenant access for an entity.
     *
     * @param tenant the tenant identifier
     * @param id     the entity ID
     * @throws TenantNotAllowedException if the tenant has no access
     */
    private Optional<T> validateTenantAccess(String tenant, I id) {
        if (isTenantAssignable) {
            Optional<T> optional = repository().findById(id);
            if (optional.isPresent()) {
                if (!TenantConstants.SUPER_TENANT_NAME.equals(tenant) && !tenant.equals(((ITenantAssignable) optional.get()).getTenant())) {
                    log.error("Tenant {} has no access to entity with ID: {}", tenant, id);
                    throw new TenantNotAllowedException("Tenant has no access to the object");
                }
                return optional;
            }
        }

        return Optional.empty();
    }

    /**
     * Handles entity deletion, supporting soft deletion for CancelableEntity.
     *
     * @param object the entity to delete
     */
    private void handleEntityDeletion(T object) {
        log.debug("Handling deletion for {} entity with ID: {}", persistentClass.getSimpleName(), object.getId());
        if (object instanceof CancelableEntity cancelable && !cancelable.getCheckCancel()) {
            cancelable.setCheckCancel(true);
            cancelable.setCancelDate(Date.from(Instant.now()));
            update(object);
        } else {
            repository().delete(object);
        }
    }
}