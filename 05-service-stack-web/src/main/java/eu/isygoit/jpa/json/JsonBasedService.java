package eu.isygoit.jpa.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceEvents;
import eu.isygoit.com.rest.service.ICrudServiceOperations;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.CreateConstraintsViolationException;
import eu.isygoit.exception.ObjectNotFoundException;
import eu.isygoit.exception.OperationNotSupportedException;
import eu.isygoit.exception.UpdateConstraintsViolationException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.helper.JsonBasedEntityHelper;
import eu.isygoit.json.JsonCriteriaQueryBuilder;
import eu.isygoit.json.JsonQueryExecutor;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.repository.json.JsonBasedRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generic JSON-based service with full CRUD support for entities stored as JSONB.
 *
 * <h3>Changes from the original</h3>
 * <ul>
 *   <li><b>Point 4 — DB criteria filtering:</b> {@code findAllByCriteriaFilter} now pushes
 *       DB-compatible operators ({@code EQ, NE, LI, NL, GT, GE, LT, LE}) directly to
 *       PostgreSQL via {@link eu.isygoit.json.JsonQueryExecutor}. Only the {@code BW} operator falls back
 *       to in-memory evaluation on the already-narrowed result set.</li>
 *   <li><b>Point 6 — Stable element type key:</b> The {@code elementType} discriminator is
 *       resolved via {@link JsonBasedEntityHelper#resolveElementType}, which checks for an
 *       {@link eu.isygoit.annotation.ElementType} annotation before falling back to the
 *       simple class name. A missing annotation produces a startup warning.</li>
 *   <li><b>Table name resolution:</b> The physical table name is resolved once at
 *       construction time from {@link jakarta.persistence.Table#name()} rather than being
 *       hardcoded in every {@code @Query}.</li>
 * </ul>
 *
 * @param <T>  JSON element type — the POJO serialised into the JSONB column
 * @param <IE> primary key type of the backing JPA entity
 * @param <E>  the JPA entity type that stores the JSONB payload
 * @param <R>  the Spring Data repository for {@code E}
 */
@Slf4j
@Transactional
public class JsonBasedService<T extends IIdAssignable<UUID> & JsonElement<UUID>,
        IE extends Serializable,
        E extends JsonBasedEntity<IE> & IIdAssignable<IE>,
        R extends JsonBasedRepository<E, IE>>
        extends CrudServiceUtils<UUID, T, R>
        implements ICrudServiceOperations<UUID, T>,
        ICrudServiceEvents<UUID, T>,
        ICrudServiceUtils<UUID, T> {

    private final Class<T> jsonElementClass;
    private final Class<E> jsonEntityClass;

    /**
     * Stable discriminator key — resolved from @ElementType or class simple name.
     */
    private final String elementType;

    /**
     * Physical table name — resolved from @Table(name=...) or class simple name.
     */
    private final String tableName;

    private final ObjectMapper objectMapper;

    /**
     * Executes dynamic JSONB criteria queries. Injected by Spring after construction;
     * field injection is used here so that concrete subclass constructors remain unchanged.
     */
    @Autowired
    private JsonQueryExecutor queryExecutor;

    /**
     * Resolves generic type parameters and derives the element type key and table name
     * from class-level metadata. Called by the Spring container when creating the
     * concrete subclass bean.
     *
     * @param objectMapper Jackson mapper used for JSONB serialisation/deserialisation
     */
    @Autowired
    public JsonBasedService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        var genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        var typeArguments = genericSuperclass.getActualTypeArguments();

        this.jsonElementClass = (Class<T>) typeArguments[0];
        this.jsonEntityClass = (Class<E>) typeArguments[2];

        // Point 6: use annotation-backed resolution instead of bare getSimpleName()
        this.elementType = JsonBasedEntityHelper.resolveElementType(jsonElementClass);
        this.tableName = JsonBasedEntityHelper.resolveTableName(jsonEntityClass);
    }

    // ── Count / exists ────────────────────────────────────────────────────────

    @Override
    public Long count() {
        return repository().countByElementType(elementType);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository().existsByElementTypeAndJsonId(elementType, id.toString());
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Override
    public T create(T object) {
        try {
            JsonBasedEntityHelper.assignIdIfNull(object);
            var beforeCreateResult = beforeCreate(object);
            var entity = JsonBasedEntityHelper.toJsonEntity(beforeCreateResult, elementType, jsonEntityClass, objectMapper);
            var saved = repository().saveAndFlush(entity);
            var result = JsonBasedEntityHelper.toJsonElement(saved, jsonElementClass, objectMapper);
            return afterCreate(result);
        } catch (DataIntegrityViolationException e) {
            throw new CreateConstraintsViolationException(e.getMessage());
        }
    }

    @Override
    public List<T> createBatch(List<T> objects) {
        validateListNotEmpty(objects);
        objects.forEach(JsonBasedEntityHelper::assignIdIfNull);
        return objects.stream()
                .map(this::beforeCreate)
                .map(obj -> JsonBasedEntityHelper.toJsonEntity(obj, elementType, jsonEntityClass, objectMapper))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        entities -> repository().saveAll(entities).stream()
                                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                                .map(this::afterCreate)
                                .toList()));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(UUID id) {
        beforeDelete(id);
        if (repository().deleteByElementTypeAndJsonId(elementType, id.toString()) == 0) {
            throw new ObjectNotFoundException("with id " + id);
        }
        afterDelete(id);
    }

    @Override
    public void deleteBatch(List<T> objects) {
        validateListNotEmpty(objects);
        beforeDelete(objects);
        var ids = objects.stream().map(e -> e.getId().toString()).toList();
        repository().deleteByElementTypeAndJsonIdIn(elementType, ids);
        afterDelete(objects);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public List<T> findAll() {
        var results = repository().findAllByElementType(elementType).stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .toList();
        return afterFindAll(results);
    }

    @Override
    public List<T> findAll(Pageable pageable) {
        var results = repository().findAllByElementType(elementType, pageable).stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .toList();
        return afterFindAll(results);
    }

    @Override
    public Optional<T> findById(UUID id) throws ObjectNotFoundException {
        return repository().findByElementTypeAndJsonId(elementType, id.toString())
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .map(this::afterFindById);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public T saveOrUpdate(T object) {
        return object.getId() == null ? create(object) : update(object);
    }

    @Override
    public List<T> saveOrUpdate(List<T> objects) {
        validateListNotEmpty(objects);
        return objects.stream().map(this::saveOrUpdate).toList();
    }

    @Override
    public T update(T object) {
        try {
            var beforeUpdateResult = beforeUpdate(object);
            var entity = findEntityById(beforeUpdateResult.getId());
            entity.update(objectMapper.valueToTree(beforeUpdateResult));
            var result = JsonBasedEntityHelper.toJsonElement(
                    repository().saveAndFlush(entity), jsonElementClass, objectMapper);
            return afterUpdate(result);
        } catch (DataIntegrityViolationException e) {
            throw new UpdateConstraintsViolationException(e.getMessage());
        }
    }

    @Override
    public List<T> updateBatch(List<T> objects) {
        validateListNotEmpty(objects);
        return objects.stream().map(this::beforeUpdate).map(this::update).toList();
    }

    // ── Criteria filtering (point 4) ──────────────────────────────────────────

    /**
     * Returns all elements matching the given criteria.
     *
     * <p>DB-pushable operators ({@code EQ, NE, LI, NL, GT, GE, LT, LE}) are translated
     * into a native JSONB query executed by {@link JsonQueryExecutor} — only matching rows
     * are transferred over the wire. Remaining operators (currently only {@code BW}) are
     * applied in-memory on the already-narrowed result set.
     */
    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            log.warn("findAllByCriteriaFilter called with no criteria — falling back to findAll.");
            return findAll();
        }

        JsonBasedEntityHelper.validateCriteriaAgainstJsonElement(jsonElementClass, criteria);

        var split = JsonCriteriaQueryBuilder.partition(criteria);
        var dbCriteria = split.getKey();
        var memCriteria = split.getValue();

        List<T> results = queryExecutor
                .findByCriteria(tableName, elementType, null, dbCriteria, jsonEntityClass)
                .stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .collect(Collectors.toList());

        if (!memCriteria.isEmpty()) {
            results = JsonBasedEntityHelper.applyCriteriaFilter(results, memCriteria, elementType);
        }

        return afterFindAll(results);
    }

    /**
     * Returns a page of elements matching the given criteria.
     *
     * <p>Pagination is applied <em>after</em> all filtering (DB and in-memory) because
     * in-memory operators may change the effective result count. When all criteria are
     * DB-pushable, consider using the repository's pageable methods directly for
     * true DB-side pagination.
     */
    @Override
    public List<T> findAllByCriteriaFilter(List<QueryCriteria> criteria, PageRequest pageRequest) {
        if (criteria == null || criteria.isEmpty()) {
            log.warn("findAllByCriteriaFilter called with no criteria — falling back to findAll with pagination.");
            return findAll(pageRequest);
        }

        JsonBasedEntityHelper.validateCriteriaAgainstJsonElement(jsonElementClass, criteria);

        var split = JsonCriteriaQueryBuilder.partition(criteria);
        var dbCriteria = split.getKey();
        var memCriteria = split.getValue();

        List<T> results = queryExecutor
                .findByCriteria(tableName, elementType, null, dbCriteria, jsonEntityClass)
                .stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .collect(Collectors.toList());

        if (!memCriteria.isEmpty()) {
            results = JsonBasedEntityHelper.applyCriteriaFilter(results, memCriteria, elementType);
        }

        return afterFindAll(JsonBasedEntityHelper.applyPagination(results, pageRequest));
    }

    @Override
    public List<T> getByIdIn(List<UUID> ids) {
        throw new OperationNotSupportedException("Json based entity: getByIdIn");
    }

    // ── Lifecycle event hooks ─────────────────────────────────────────────────

    @Override
    public T beforeUpdate(T object) {
        log.debug("Before update {} [id={}]", elementType, object.getId());
        return object;
    }

    @Override
    public T afterUpdate(T object) {
        log.debug("After update {} [id={}]", elementType, object.getId());
        return object;
    }

    @Override
    public void beforeDelete(UUID id) {
        log.debug("Before delete {} [id={}]", elementType, id);
    }

    @Override
    public void afterDelete(UUID id) {
        log.debug("After delete {} [id={}]", elementType, id);
    }

    @Override
    public void beforeDelete(List<T> objects) {
        log.debug("Before delete {} batch [{} items]", elementType, objects.size());
    }

    @Override
    public void afterDelete(List<T> objects) {
        log.debug("After delete {} batch [{} items]", elementType, objects.size());
    }

    @Override
    public T beforeCreate(T object) {
        log.debug("Before create {} [id={}]", elementType, object.getId());
        return object;
    }

    @Override
    public List<T> afterFindAll(List<T> list) {
        log.debug("After find all {} [{} items]", elementType, list.size());
        return list;
    }

    @Override
    public T afterFindById(T object) {
        log.debug("After find by id {} [id={}]", elementType, object.getId());
        return object;
    }

    @Override
    public T afterCreate(T object) {
        log.debug("After create {} [id={}]", elementType, object.getId());
        return object;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private E findEntityById(UUID id) {
        return repository().findByElementTypeAndJsonId(elementType, id.toString())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Entity not found for type='%s' and id='%s'".formatted(elementType, id)));
    }
}