package eu.isygoit.jpa.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceEvents;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceMethods;
import eu.isygoit.exception.*;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.helper.JsonBasedEntityHelper;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.repository.json.JsonBasedTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generic tenant-aware JSON-based api implementation with CRUD operations.
 * Provides multi-tenant support for entities stored as JSON in the database.
 *
 * @param <T>  JSON element type extending IIdAssignable and JsonElement
 * @param <IE> ID type for the entity (Serializable)
 * @param <E>  JSON entity type extending JsonBasedEntity, IIdAssignable, and ITenantAssignable
 * @param <R>  Repository type extending JsonBasedTenantAssignableRepository
 */
@Slf4j
@Transactional
public class JsonBasedTenantService<T extends IIdAssignable<UUID> & JsonElement<UUID> & ITenantAssignable,
        IE extends Serializable,
        E extends JsonBasedEntity<IE> & IIdAssignable<IE> & ITenantAssignable,
        R extends JsonBasedTenantAssignableRepository<E, IE>>
        extends CrudServiceUtils<UUID, T, R>
        implements ICrudTenantServiceMethods<UUID, T>,
        ICrudTenantServiceEvents<UUID, T>,
        ICrudServiceUtils<UUID, T> {

    private final Class<T> jsonElementClass;
    private final Class<E> jsonEntityClass;
    private final String elementType;
    private final ObjectMapper objectMapper;

    /**
     * Instantiates a new Json based tenant api.
     *
     * @param objectMapper the object mapper
     */
    @Autowired
    public JsonBasedTenantService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        var genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        var typeArguments = genericSuperclass.getActualTypeArguments();
        this.jsonElementClass = (Class<T>) typeArguments[0];
        this.jsonEntityClass = (Class<E>) typeArguments[2];
        this.elementType = jsonElementClass.getSimpleName().toUpperCase();
    }

    @Override
    public Long count(String tenant) {
        validateTenant(tenant);
        return repository().countByElementTypeAndTenant(elementType, tenant);
    }

    @Override
    public boolean existsById(String tenant, UUID id) {
        validateTenant(tenant);
        return repository().existsByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant);
    }

    @Override
    public T create(String tenant, T object) {
        try {
            validateTenant(tenant);
            JsonBasedEntityHelper.assignIdIfNull(object);
            var beforeCreateResult = beforeCreate(tenant, object);
            var entity = JsonBasedEntityHelper.toJsonEntity(beforeCreateResult, elementType, jsonEntityClass, objectMapper);
            entity.setTenant(tenant);
            var saved = repository().save(entity);
            var result = JsonBasedEntityHelper.toJsonElement(saved, jsonElementClass, objectMapper);
            return afterCreate(tenant, result);
        } catch (DataIntegrityViolationException e) {
            throw new CreateConstraintsViolationException(e.getMessage());
        }
    }

    @Override
    public List<T> createBatch(String tenant, List<T> objects) {
        validateTenant(tenant);
        validateListNotEmpty(objects);
        objects.forEach(JsonBasedEntityHelper::assignIdIfNull);
        var beforeCreateResults = objects.stream()
                .map(t -> beforeCreate(tenant, t))
                .toList();
        var entities = beforeCreateResults.stream()
                .map(obj -> {
                    var entity = JsonBasedEntityHelper.toJsonEntity(obj, elementType, jsonEntityClass, objectMapper);
                    entity.setTenant(tenant);
                    return entity;
                })
                .toList();
        return repository().saveAll(entities)
                .stream()
                .map(entity -> JsonBasedEntityHelper.toJsonElement(entity, jsonElementClass, objectMapper))
                .map(t -> afterCreate(tenant, t))
                .toList();
    }

    @Override
    public void delete(String tenant, UUID id) {
        validateTenant(tenant);
        beforeDelete(tenant, id);
        if (repository().deleteByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant) == 0) {
            throw new ObjectNotFoundException("with id " + id + " and tenant " + tenant);
        }
        afterDelete(tenant, id);
    }

    @Override
    public void deleteBatch(String tenant, List<T> objects) {
        validateTenant(tenant);
        validateListNotEmpty(objects);
        beforeDelete(tenant, objects);
        var ids = objects.stream()
                .map(entity -> entity.getId().toString())
                .toList();
        repository().deleteByElementTypeAndJsonIdInAndTenant(elementType, ids, tenant);
        afterDelete(tenant, objects);
    }

    @Override
    public List<T> findAll(String tenant) {
        validateTenant(tenant);
        var results = repository().findAllByElementTypeAndTenant(elementType, tenant)
                .stream()
                .map(entity -> JsonBasedEntityHelper.toJsonElement(entity, jsonElementClass, objectMapper))
                .toList();
        return afterFindAll(tenant, results);
    }

    @Override
    public List<T> findAll(String tenant, Pageable pageable) {
        validateTenant(tenant);
        var results = repository().findAllByElementTypeAndTenant(elementType, tenant, pageable)
                .stream()
                .map(entity -> JsonBasedEntityHelper.toJsonElement(entity, jsonElementClass, objectMapper))
                .toList();
        return afterFindAll(tenant, results);
    }

    @Override
    public Optional<T> findById(String tenant, UUID id) throws ObjectNotFoundException {
        validateTenant(tenant);
        return repository().findByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant)
                .map(entity -> JsonBasedEntityHelper.toJsonElement(entity, jsonElementClass, objectMapper))
                .map(t -> afterFindById(tenant, t));
    }

    @Override
    public T saveOrUpdate(String tenant, T object) {
        validateTenant(tenant);
        return object.getId() == null ? create(tenant, object) : update(tenant, object);
    }

    @Override
    public List<T> saveOrUpdate(String tenant, List<T> objects) {
        validateTenant(tenant);
        validateListNotEmpty(objects);
        return objects.stream()
                .map(obj -> saveOrUpdate(tenant, obj))
                .toList();
    }

    @Override
    public T update(String tenant, T object) {
        try {
            validateTenant(tenant);
            var beforeUpdateResult = beforeUpdate(tenant, object);
            var entity = findEntityById(tenant, beforeUpdateResult.getId());
            entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
            var result = JsonBasedEntityHelper.toJsonElement(repository().saveAndFlush(entity), jsonElementClass, objectMapper);
            return afterUpdate(tenant, result);
        } catch (DataIntegrityViolationException e) {
            throw new UpdateConstraintsViolationException(e.getMessage());
        }
    }

    @Override
    public List<T> updateBatch(String tenant, List<T> objects) {
        validateTenant(tenant);
        validateListNotEmpty(objects);
        return objects.stream()
                .map(t -> beforeUpdate(tenant, t))
                .map(obj -> update(tenant, obj))
                .toList();
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria) {
        validateTenant(tenant);

        if (criteria == null || criteria.isEmpty()) {
            log.warn("No criteria provided, falling back to findAll for tenant: {}", tenant);
            return findAll(tenant);
        }

        JsonBasedEntityHelper.validateCriteriaAgainstJsonElement(jsonElementClass, criteria);
        List<T> entities = repository().findAllByElementTypeAndTenant(elementType, tenant)
                .stream()
                .map(entity -> JsonBasedEntityHelper.toJsonElement(entity, jsonElementClass, objectMapper))
                .collect(Collectors.toList());
        List<T> filtered = JsonBasedEntityHelper.applyCriteriaFilter(entities, criteria, elementType);
        return afterFindAll(tenant, filtered);
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest) {
        validateTenant(tenant);

        if (criteria == null || criteria.isEmpty()) {
            log.warn("No criteria provided, falling back to findAll with pagination for tenant: {}", tenant);
            return findAll(tenant, pageRequest);
        }

        JsonBasedEntityHelper.validateCriteriaAgainstJsonElement(jsonElementClass, criteria);
        List<T> entities = repository().findAllByElementTypeAndTenant(elementType, tenant)
                .stream()
                .map(entity -> JsonBasedEntityHelper.toJsonElement(entity, jsonElementClass, objectMapper))
                .collect(Collectors.toList());
        List<T> filtered = JsonBasedEntityHelper.applyCriteriaFilter(entities, criteria, elementType);
        List<T> paginated = JsonBasedEntityHelper.applyPagination(filtered, pageRequest);
        return afterFindAll(tenant, paginated);
    }

    @Override
    public List<T> getByIdIn(List<UUID> ids) {
        throw new OperationNotSupportedException("Json based entity: getByIdIn");
    }

    // Event lifecycle methods
    @Override
    public T beforeUpdate(String tenant, T object) {
        log.debug("Before update {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public T afterUpdate(String tenant, T object) {
        log.debug("After update {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public void beforeDelete(String tenant, UUID id) {
        log.debug("Before delete {} [{}]: {}", elementType, "id", id);
    }

    @Override
    public void afterDelete(String tenant, UUID id) {
        log.debug("After delete {} [{}]: {}", elementType, "id", id);
    }

    @Override
    public void beforeDelete(String tenant, List<T> objects) {
        log.debug("Before delete {} batch [{}]: {} items", elementType, "size", objects.size());
    }

    @Override
    public void afterDelete(String tenant, List<T> objects) {
        log.debug("After delete {} batch [{}]: {} items", elementType, "size", objects.size());
    }

    @Override
    public T beforeCreate(String tenant, T object) {
        log.debug("Before create {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public List<T> afterFindAll(String tenant, List<T> list) {
        log.debug("After find all {} [{}]: {} items", elementType, "size", list.size());
        return list;
    }

    @Override
    public T afterFindById(String tenant, T object) {
        log.debug("After find by id {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public T afterCreate(String tenant, T object) {
        log.debug("After create {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    // Tenant-specific helper methods
    private void validateTenant(String tenant) {
        if (!StringUtils.hasText(tenant)) {
            throw new InvalidTenantException("Tenant cannot be null or empty");
        }
    }

    private E findEntityById(String tenant, UUID id) {
        return repository().findByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Entity not found for type: %s, id: %s and tenant: %s".formatted(elementType, id, tenant)));
    }
}