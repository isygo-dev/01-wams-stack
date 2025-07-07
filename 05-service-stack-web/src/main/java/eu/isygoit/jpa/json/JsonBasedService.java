package eu.isygoit.jpa.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceMethods;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.repository.json.JsonBasedRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Generic JSON-based service implementation with CRUD operations.
 * Provides a flexible way to handle entities stored as JSON in the database.
 *
 * @param <T>  JSON element type extending IIdAssignable and JsonElement
 * @param <IE> ID type for the entity (Serializable)
 * @param <E>  JSON entity type extending JsonBasedEntity and IIdAssignable
 * @param <R>  Repository type extending JsonBasedRepository
 */
@Slf4j
@Transactional
public class JsonBasedService<T extends IIdAssignable<UUID> & JsonElement<UUID>,
        IE extends Serializable,
        E extends JsonBasedEntity<IE> & IIdAssignable<IE>,
        R extends JsonBasedRepository<E, IE>>
        extends CrudServiceUtils<UUID, T, R>
        implements ICrudServiceMethods<UUID, T>,
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
    public JsonBasedService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        // Extract generic type arguments with better error handling
        var genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        var typeArguments = genericSuperclass.getActualTypeArguments();

        this.jsonElementClass = (Class<T>) typeArguments[0];
        this.jsonEntityClass = (Class<E>) typeArguments[2];
        this.elementType = jsonElementClass.getSimpleName().toUpperCase();
    }

    @Override
    public Long count() {
        return repository().countByElementType(elementType);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository().existsByElementTypeAndJsonId(elementType, id.toString());
    }

    @Override
    public T create(T object) {
        assignIdIfNull(object);

        var beforeCreateResult = beforeCreate(object);
        var entity = toJsonEntity(beforeCreateResult);
        var saved = repository().save(entity);
        var result = toJsonElement(saved);

        return afterCreate(result);
    }

    @Override
    public T createAndFlush(T object) {
        assignIdIfNull(object);

        var beforeCreateResult = beforeCreate(object);
        var entity = toJsonEntity(beforeCreateResult);
        var saved = repository().saveAndFlush(entity);
        var result = toJsonElement(saved);

        return afterCreate(result);
    }

    @Override
    public List<T> createBatch(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return List.of();
        }

        // Assign IDs to objects that don't have them
        objects.forEach(this::assignIdIfNull);

        var beforeCreateResults = objects.stream()
                .map(this::beforeCreate)
                .toList();

        var entities = beforeCreateResults.stream()
                .map(this::toJsonEntity)
                .toList();

        return repository().saveAll(entities)
                .stream()
                .map(this::toJsonElement)
                .map(this::afterCreate)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        beforeDelete(id);
        repository().deleteByElementTypeAndJsonId(elementType, id.toString());
        afterDelete(id);
    }

    @Override
    public void deleteBatch(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return;
        }

        beforeDelete(objects);

        var ids = objects.stream()
                .map(entity -> entity.getId().toString())
                .toList();

        repository().deleteByElementTypeAndJsonIdIn(elementType, ids);
        afterDelete(objects);
    }

    @Override
    public List<T> findAll() {
        var results = repository().findAllByElementType(elementType)
                .stream()
                .map(this::toJsonElement)
                .toList();

        return afterFindAll(results);
    }

    @Override
    public List<T> findAll(Pageable pageable) {
        var results = repository().findAllByElementType(elementType, pageable)
                .stream()
                .map(this::toJsonElement)
                .toList();

        return afterFindAll(results);
    }

    @Override
    public Optional<T> findById(UUID id) throws ObjectNotFoundException {
        return repository().findByElementTypeAndJsonId(elementType, id.toString())
                .map(this::toJsonElement)
                .map(this::afterFindById);
    }

    @Override
    public T saveOrUpdate(T object) {
        return object.getId() == null ? create(object) : update(object);
    }

    @Override
    public List<T> saveOrUpdate(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return List.of();
        }

        return objects.stream()
                .map(this::saveOrUpdate)
                .toList();
    }

    @Override
    public T update(T object) {
        var beforeUpdateResult = beforeUpdate(object);
        var entity = findEntityById(beforeUpdateResult.getId());

        entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
        var result = toJsonElement(repository().save(entity));

        return afterUpdate(result);
    }

    @Override
    public T updateAndFlush(T object) {
        var beforeUpdateResult = beforeUpdate(object);
        var entity = findEntityById(beforeUpdateResult.getId());

        entity.setAttributes(objectMapper.valueToTree(beforeUpdateResult));
        var result = toJsonElement(repository().saveAndFlush(entity));

        return afterUpdate(result);
    }

    @Override
    public List<T> updateBatch(List<T> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return List.of();
        }

        return objects.stream()
                .map(this::beforeUpdate)
                .map(this::update)
                .toList();
    }

    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria) {
        // TODO: Implement dynamic filtering using JSON criteria
        log.warn("Criteria filtering not yet implemented, falling back to findAll()");
        return findAll();
    }

    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest) {
        // TODO: Implement dynamic filtering using JSON criteria + pagination
        log.warn("Criteria filtering with pagination not yet implemented, falling back to findAll()");
        return findAll(pageRequest);
    }

    // Event lifecycle methods with improved logging
    @Override
    public T beforeUpdate(T object) {
        log.debug("Before update {}: {}", elementType, object.getId());
        return object;
    }

    @Override
    public T afterUpdate(T object) {
        log.debug("After update {}: {}", elementType, object.getId());
        return object;
    }

    @Override
    public void beforeDelete(UUID id) {
        log.debug("Before delete {}: {}", elementType, id);
    }

    @Override
    public void afterDelete(UUID id) {
        log.debug("After delete {}: {}", elementType, id);
    }

    @Override
    public void beforeDelete(List<T> objects) {
        log.debug("Before delete {} batch: {} items", elementType, objects.size());
    }

    @Override
    public void afterDelete(List<T> objects) {
        log.debug("After delete {} batch: {} items", elementType, objects.size());
    }

    @Override
    public T beforeCreate(T object) {
        log.debug("Before create {}: {}", elementType, object.getId());
        return object;
    }

    @Override
    public List<T> afterFindAll(List<T> list) {
        log.debug("After find all {}: {} items", elementType, list.size());
        return list;
    }

    @Override
    public T afterFindById(T object) {
        log.debug("After find by id {}: {}", elementType, object.getId());
        return object;
    }

    @Override
    public T afterCreate(T object) {
        log.debug("After create {}: {}", elementType, object.getId());
        return object;
    }

    // Helper methods
    private void assignIdIfNull(T object) {
        if (object.getId() == null) {
            object.setId(UUID.randomUUID());
        }
    }

    private E findEntityById(UUID id) {
        return repository().findByElementTypeAndJsonId(elementType, id.toString())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Entity not found for type: %s and id: %s".formatted(elementType, id)));
    }

    private E toJsonEntity(T element) {
        var json = objectMapper.valueToTree(element);
        try {
            var jsonEntity = jsonEntityClass.getDeclaredConstructor().newInstance();
            jsonEntity.setElementType(elementType);
            jsonEntity.setAttributes(json);
            return jsonEntity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create %s instance".formatted(elementType), e);
        }
    }

    private T toJsonElement(E jsonEntity) {
        return objectMapper.convertValue(jsonEntity.getAttributes(), jsonElementClass);
    }
}