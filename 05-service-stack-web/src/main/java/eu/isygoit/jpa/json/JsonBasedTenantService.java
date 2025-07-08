package eu.isygoit.jpa.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudTenantServiceMethods;
import eu.isygoit.exception.InvalidTenantException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.helper.CriteriaHelper;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.repository.json.JsonBasedTenantAssignableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Generic tenant-aware JSON-based service implementation with CRUD operations.
 * Provides multi-tenant support for entities stored as JSON in the database.
 *
 * @param <T>  JSON element type extending IIdAssignable and JsonElement
 * @param <IE> ID type for the entity (Serializable)
 * @param <E>  JSON entity type extending JsonBasedEntity, IIdAssignable, and ITenantAssignable
 * @param <R>  Repository type extending JsonBasedTenantAssignableRepository
 */
@Slf4j
@Transactional
public class JsonBasedTenantService<T extends IIdAssignable<UUID> & JsonElement<UUID>,
        IE extends Serializable,
        E extends JsonBasedEntity<IE> & IIdAssignable<IE> & ITenantAssignable,
        R extends JsonBasedTenantAssignableRepository<E, IE>>
        extends CrudServiceUtils<UUID, T, R>
        implements ICrudTenantServiceMethods<UUID, T>,
        ICrudServiceEvents<UUID, T>,
        ICrudServiceUtils<UUID, T> {

    // Cache the class types for better performance
    private final Class<T> jsonElementClass;
    private final Class<E> jsonEntityClass;
    private final String elementType;

    private final ObjectMapper objectMapper;

    /**
     * Constructor that initializes class types and element type.
     * Using constructor injection for better testability.
     */
    @Autowired
    public JsonBasedTenantService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // Extract generic type arguments with better error handling
        var genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        var typeArguments = genericSuperclass.getActualTypeArguments();

        this.jsonElementClass = (Class<T>) typeArguments[0];
        this.jsonEntityClass = (Class<E>) typeArguments[2];
        this.elementType = jsonElementClass.getSimpleName().toUpperCase();
    }

    // Tenant-aware methods implementation
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
        validateTenant(tenant);
        assignIdIfNull(object);

        var beforeCreateResult = beforeCreate(object);
        var entity = toJsonEntity(beforeCreateResult, tenant);
        var saved = repository().save(entity);
        var result = toJsonElement(saved);

        return afterCreate(result);
    }

    @Override
    public T createAndFlush(String tenant, T object) {
        validateTenant(tenant);
        assignIdIfNull(object);

        var beforeCreateResult = beforeCreate(object);
        var entity = toJsonEntity(beforeCreateResult, tenant);
        var saved = repository().saveAndFlush(entity);
        var result = toJsonElement(saved);

        return afterCreate(result);
    }

    @Override
    public List<T> createBatch(String tenant, List<T> objects) {
        validateTenant(tenant);

        validateListNotEmpty(objects);

        // Assign IDs to objects that don't have them
        objects.forEach(this::assignIdIfNull);

        var beforeCreateResults = objects.stream()
                .map(this::beforeCreate)
                .toList();

        var entities = beforeCreateResults.stream()
                .map(obj -> toJsonEntity(obj, tenant))
                .toList();

        return repository().saveAll(entities)
                .stream()
                .map(this::toJsonElement)
                .map(this::afterCreate)
                .toList();
    }

    @Override
    public void delete(String tenant, UUID id) {
        validateTenant(tenant);
        beforeDelete(id);
        if (repository().deleteByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant) == 0) {
            throw new ObjectNotFoundException("with id " + id + " and tenant " + tenant);
        }
        afterDelete(id);
    }

    @Override
    public void deleteBatch(String tenant, List<T> objects) {
        validateTenant(tenant);

        validateListNotEmpty(objects);

        beforeDelete(objects);

        var ids = objects.stream()
                .map(entity -> entity.getId().toString())
                .toList();

        repository().deleteByElementTypeAndJsonIdInAndTenant(elementType, ids, tenant);
        afterDelete(objects);
    }

    @Override
    public List<T> findAll(String tenant) {
        validateTenant(tenant);

        var results = repository().findAllByElementTypeAndTenant(elementType, tenant)
                .stream()
                .map(this::toJsonElement)
                .toList();

        return afterFindAll(results);
    }

    @Override
    public List<T> findAll(String tenant, Pageable pageable) {
        validateTenant(tenant);

        var results = repository().findAllByElementTypeAndTenant(elementType, tenant, pageable)
                .stream()
                .map(this::toJsonElement)
                .toList();

        return afterFindAll(results);
    }

    @Override
    public Optional<T> findById(String tenant, UUID id) throws ObjectNotFoundException {
        validateTenant(tenant);

        return repository().findByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant)
                .map(this::toJsonElement)
                .map(this::afterFindById);
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
        validateTenant(tenant);

        var beforeUpdateResult = beforeUpdate(object);
        var entity = findEntityById(tenant, beforeUpdateResult.getId());

        entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
        var result = toJsonElement(repository().save(entity));

        return afterUpdate(result);
    }

    @Override
    public T updateAndFlush(String tenant, T object) {
        validateTenant(tenant);

        var beforeUpdateResult = beforeUpdate(object);
        var entity = findEntityById(tenant, beforeUpdateResult.getId());

        entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
        var result = toJsonElement(repository().saveAndFlush(entity));

        return afterUpdate(result);
    }

    @Override
    public List<T> updateBatch(String tenant, List<T> objects) {
        validateTenant(tenant);

        validateListNotEmpty(objects);

        return objects.stream()
                .map(this::beforeUpdate)
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

        // Validate criteria names against jsonElementClass fields
        validateCriteriaAgainstJsonElement(criteria);

        log.debug("Filtering {} entities for tenant: {} with {} criteria", elementType, tenant, criteria.size());

        // Fetch all entities and convert to JsonElement
        List<T> entities = repository().findAllByElementTypeAndTenant(elementType, tenant)
                .stream()
                .map(this::toJsonElement)
                .collect(Collectors.toList());

        // Apply in-memory filtering
        List<T> filtered = entities.stream()
                .filter(entity -> JsonBasedEntityHelper.evaluateCriteria(entity, criteria))
                .collect(Collectors.toList());

        log.debug("Found {} {} entities for tenant: {}", filtered.size(), elementType, tenant);
        return afterFindAll(filtered);
    }

    private void validateCriteriaAgainstJsonElement(List<QueryCriteria> criteria) {
        var validFields = CriteriaHelper.getCriteriaData(jsonElementClass);
        for (QueryCriteria criterion : criteria) {
            if (!validFields.containsKey(criterion.getName())) {
                throw new WrongCriteriaFilterException("with name: " + criterion.getName());
            }
        }
    }

    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria, PageRequest pageRequest) {
        validateTenant(tenant);
        if (criteria == null || criteria.isEmpty()) {
            log.warn("No criteria provided, falling back to findAll with pagination for tenant: {}", tenant);
            return findAll(tenant, pageRequest);
        }

        // Validate criteria names against jsonElementClass fields
        validateCriteriaAgainstJsonElement(criteria);

        log.debug("Filtering {} entities for tenant: {} with {} criteria and pagination", elementType, tenant, criteria.size());

        // Fetch all entities and convert to JsonElement
        List<T> entities = repository().findAllByElementTypeAndTenant(elementType, tenant)
                .stream()
                .map(this::toJsonElement)
                .collect(Collectors.toList());

        // Apply in-memory filtering
        List<T> filtered = entities.stream()
                .filter(entity -> JsonBasedEntityHelper.evaluateCriteria(entity, criteria))
                .collect(Collectors.toList());

        // Apply pagination
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), filtered.size());
        List<T> paginated = start < filtered.size() ? filtered.subList(start, end) : List.of();

        log.debug("Found {} {} entities for tenant: {} with pagination", paginated.size(), elementType, tenant);
        return afterFindAll(paginated);
    }

    // Event lifecycle methods with improved logging
    @Override
    public T beforeUpdate(T object) {
        log.debug("Before update {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public T afterUpdate(T object) {
        log.debug("After update {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public void beforeDelete(UUID id) {
        log.debug("Before delete {} [{}]: {}", elementType, "id", id);
    }

    @Override
    public void afterDelete(UUID id) {
        log.debug("After delete {} [{}]: {}", elementType, "id", id);
    }

    @Override
    public void beforeDelete(List<T> objects) {
        log.debug("Before delete {} batch [{}]: {} items", elementType, "size", objects.size());
    }

    @Override
    public void afterDelete(List<T> objects) {
        log.debug("After delete {} batch [{}]: {} items", elementType, "size", objects.size());
    }

    @Override
    public T beforeCreate(T object) {
        log.debug("Before create {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public List<T> afterFindAll(List<T> list) {
        log.debug("After find all {} [{}]: {} items", elementType, "size", list.size());
        return list;
    }

    @Override
    public T afterFindById(T object) {
        log.debug("After find by id {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    @Override
    public T afterCreate(T object) {
        log.debug("After create {} [{}]: {}", elementType, "id", object.getId());
        return object;
    }

    // Helper methods
    private void validateTenant(String tenant) {
        if (!StringUtils.hasText(tenant)) {
            throw new InvalidTenantException("Tenant cannot be null or empty");
        }
    }

    private void assignIdIfNull(T object) {
        if (object.getId() == null) {
            object.setId(UUID.randomUUID());
        }
    }

    private E findEntityById(String tenant, UUID id) {
        return repository().findByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Entity not found for type: %s, id: %s and tenant: %s".formatted(elementType, id, tenant)));
    }

    private E toJsonEntity(T element, String tenant) {
        var json = objectMapper.valueToTree(element);
        try {
            var jsonEntity = jsonEntityClass.getDeclaredConstructor().newInstance();

            // Set tenant if the entity supports it
            if (ITenantAssignable.class.isAssignableFrom(jsonEntityClass)) {
                jsonEntity.setTenant(tenant);
            }

            jsonEntity.setElementType(elementType);
            jsonEntity.setAttributes(json);
            return jsonEntity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create %s instance for tenant %s".formatted(elementType, tenant), e);
        }
    }

    private T toJsonElement(E jsonEntity) {
        return objectMapper.convertValue(jsonEntity.getAttributes(), jsonElementClass);
    }
}