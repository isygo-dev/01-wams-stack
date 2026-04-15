package eu.isygoit.jpa.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.com.rest.service.CrudServiceUtils;
import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceEvents;
import eu.isygoit.com.rest.service.tenancy.ICrudTenantServiceOperations;
import eu.isygoit.exception.*;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.helper.JsonBasedEntityHelper;
import eu.isygoit.json.JsonCriteriaQueryBuilder;
import eu.isygoit.json.JsonQueryExecutor;
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
 * Tenant-aware counterpart of {@link JsonBasedService}.
 * Every operation is scoped to a tenant and requires a non-blank tenant value.
 *
 * <h3>Changes from the original</h3>
 * <ul>
 *   <li><b>Point 4 — DB criteria filtering:</b> {@code findAllByCriteriaFilter} pushes
 *       DB-compatible operators to PostgreSQL via {@link eu.isygoit.json.JsonQueryExecutor}, including
 *       the tenant predicate in the same query. Only {@code BW} falls back to
 *       in-memory evaluation on the already-narrowed result set.</li>
 *   <li><b>Point 6 — Stable element type key:</b> The {@code elementType} discriminator
 *       is resolved via {@link JsonBasedEntityHelper#resolveElementType}, checking for
 *       {@link eu.isygoit.annotation.ElementType} before falling back to the simple class
 *       name. A missing annotation produces a startup warning.</li>
 *   <li><b>Table name resolution:</b> Resolved once at construction from
 *       {@link jakarta.persistence.Table#name()} rather than hardcoded in queries.</li>
 * </ul>
 *
 * @param <T>  JSON element type — must also implement {@link ITenantAssignable}
 * @param <IE> primary key type of the backing JPA entity
 * @param <E>  the JPA entity type that stores the JSONB payload
 * @param <R>  the Spring Data repository for {@code E}
 */
@Slf4j
@Transactional
public class JsonBasedTenantService<T extends IIdAssignable<UUID> & JsonElement<UUID> & ITenantAssignable,
        IE extends Serializable,
        E extends JsonBasedEntity<IE> & IIdAssignable<IE> & ITenantAssignable,
        R extends JsonBasedTenantAssignableRepository<E, IE>>
        extends CrudServiceUtils<UUID, T, R>
        implements ICrudTenantServiceOperations<UUID, T>,
        ICrudTenantServiceEvents<UUID, T>,
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
     * Executes dynamic JSONB criteria queries with tenant scoping.
     * Field injection keeps concrete subclass constructors unchanged.
     */
    @Autowired
    private JsonQueryExecutor queryExecutor;

    /**
     * Resolves generic type parameters and derives the element type key and table name
     * from class-level metadata.
     *
     * @param objectMapper Jackson mapper used for JSONB serialisation/deserialisation
     */
    @Autowired
    public JsonBasedTenantService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        var genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        var typeArguments = genericSuperclass.getActualTypeArguments();

        this.jsonElementClass = (Class<T>) typeArguments[0];
        this.jsonEntityClass = (Class<E>) typeArguments[2];

        // Point 6: annotation-backed resolution instead of bare getSimpleName()
        this.elementType = JsonBasedEntityHelper.resolveElementType(jsonElementClass);
        this.tableName = JsonBasedEntityHelper.resolveTableName(jsonEntityClass);
    }

    // ── Count / exists ────────────────────────────────────────────────────────

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

    // ── Create ────────────────────────────────────────────────────────────────

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
        var entities = objects.stream()
                .map(t -> beforeCreate(tenant, t))
                .map(obj -> {
                    var entity = JsonBasedEntityHelper.toJsonEntity(obj, elementType, jsonEntityClass, objectMapper);
                    entity.setTenant(tenant);
                    return entity;
                })
                .toList();
        return repository().saveAll(entities).stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .map(t -> afterCreate(tenant, t))
                .toList();
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Override
    public void delete(String tenant, UUID id) {
        validateTenant(tenant);
        beforeDelete(tenant, id);
        if (repository().deleteByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant) == 0) {
            throw new ObjectNotFoundException(
                    "with id '%s' and tenant '%s'".formatted(id, tenant));
        }
        afterDelete(tenant, id);
    }

    @Override
    public void deleteBatch(String tenant, List<T> objects) {
        validateTenant(tenant);
        validateListNotEmpty(objects);
        beforeDelete(tenant, objects);
        var ids = objects.stream().map(e -> e.getId().toString()).toList();
        repository().deleteByElementTypeAndJsonIdInAndTenant(elementType, ids, tenant);
        afterDelete(tenant, objects);
    }

    // ── Read ──────────────────────────────────────────────────────────────────

    @Override
    public List<T> findAll(String tenant) {
        validateTenant(tenant);
        var results = repository().findAllByElementTypeAndTenant(elementType, tenant).stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .toList();
        return afterFindAll(tenant, results);
    }

    @Override
    public List<T> findAll(String tenant, Pageable pageable) {
        validateTenant(tenant);
        var results = repository().findAllByElementTypeAndTenant(elementType, tenant, pageable).stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .toList();
        return afterFindAll(tenant, results);
    }

    @Override
    public Optional<T> findById(String tenant, UUID id) throws ObjectNotFoundException {
        validateTenant(tenant);
        return repository().findByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant)
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .map(t -> afterFindById(tenant, t));
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Override
    public T saveOrUpdate(String tenant, T object) {
        validateTenant(tenant);
        return object.getId() == null ? create(tenant, object) : update(tenant, object);
    }

    @Override
    public List<T> saveOrUpdate(String tenant, List<T> objects) {
        validateTenant(tenant);
        validateListNotEmpty(objects);

        List<T> toCreate = objects.stream()
                .filter(obj -> obj.getId() == null)
                .toList();
        List<T> toUpdate = objects.stream()
                .filter(obj -> obj.getId() != null)
                .toList();

        List<T> result = new java.util.ArrayList<>();
        if (!toCreate.isEmpty()) {
            result.addAll(createBatch(tenant, toCreate));
        }
        if (!toUpdate.isEmpty()) {
            result.addAll(updateBatch(tenant, toUpdate));
        }

        return result;
    }

    @Override
    public T update(String tenant, T object) {
        try {
            validateTenant(tenant);
            var beforeUpdateResult = beforeUpdate(tenant, object);
            var entity = findEntityById(tenant, beforeUpdateResult.getId());
            entity.update(objectMapper.valueToTree(beforeUpdateResult));
            var result = JsonBasedEntityHelper.toJsonElement(
                    repository().saveAndFlush(entity), jsonElementClass, objectMapper);
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
                .map(obj -> {
                    var entity = findEntityById(tenant, obj.getId());
                    entity.update(objectMapper.valueToTree(obj));
                    return entity;
                })
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        entities -> repository().saveAll(entities).stream()
                                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                                .map(t -> afterUpdate(tenant, t))
                                .toList()));
    }

    // ── Criteria filtering (point 4) ──────────────────────────────────────────

    /**
     * Returns all elements for the given tenant matching the criteria.
     *
     * <p>DB-pushable operators are sent to PostgreSQL as JSONB predicates alongside
     * the tenant filter — both constraints are evaluated in one query. Only
     * {@code BW} is handled in-memory on the narrowed result set.
     */
    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria) {
        validateTenant(tenant);

        if (criteria == null || criteria.isEmpty()) {
            log.warn("findAllByCriteriaFilter called with no criteria — falling back to findAll for tenant '{}'.", tenant);
            return findAll(tenant);
        }

        JsonBasedEntityHelper.validateCriteriaAgainstJsonElement(jsonElementClass, criteria);

        var split = JsonCriteriaQueryBuilder.partition(criteria);
        var dbCriteria = split.getKey();
        var memCriteria = split.getValue();

        List<T> results = queryExecutor
                .findByCriteria(tableName, elementType, tenant, dbCriteria, jsonEntityClass)
                .stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .collect(Collectors.toList());

        if (!memCriteria.isEmpty()) {
            results = JsonBasedEntityHelper.applyCriteriaFilter(results, memCriteria, elementType);
        }

        return afterFindAll(tenant, results);
    }

    /**
     * Returns a page of elements for the given tenant matching the criteria.
     *
     * <p>Pagination is applied after all filtering because in-memory operators
     * may change the effective result count after the DB fetch.
     */
    @Override
    public List<T> findAllByCriteriaFilter(String tenant, List<QueryCriteria> criteria,
                                           PageRequest pageRequest) {
        validateTenant(tenant);

        if (criteria == null || criteria.isEmpty()) {
            log.warn("findAllByCriteriaFilter called with no criteria — falling back to findAll with pagination for tenant '{}'.", tenant);
            return findAll(tenant, pageRequest);
        }

        JsonBasedEntityHelper.validateCriteriaAgainstJsonElement(jsonElementClass, criteria);

        var split = JsonCriteriaQueryBuilder.partition(criteria);
        var dbCriteria = split.getKey();
        var memCriteria = split.getValue();

        List<T> results = queryExecutor
                .findByCriteria(tableName, elementType, tenant, dbCriteria, jsonEntityClass)
                .stream()
                .map(e -> JsonBasedEntityHelper.toJsonElement(e, jsonElementClass, objectMapper))
                .collect(Collectors.toList());

        if (!memCriteria.isEmpty()) {
            results = JsonBasedEntityHelper.applyCriteriaFilter(results, memCriteria, elementType);
        }

        return afterFindAll(tenant, JsonBasedEntityHelper.applyPagination(results, pageRequest));
    }

    @Override
    public List<T> getByIdIn(List<UUID> ids) {
        throw new OperationNotSupportedException("Json based entity: getByIdIn");
    }

    // ── Lifecycle event hooks ─────────────────────────────────────────────────

    @Override
    public T beforeUpdate(String tenant, T object) {
        log.debug("Before update {} [tenant='{}', id={}]", elementType, tenant, object.getId());
        return object;
    }

    @Override
    public T afterUpdate(String tenant, T object) {
        log.debug("After update {} [tenant='{}', id={}]", elementType, tenant, object.getId());
        return object;
    }

    @Override
    public void beforeDelete(String tenant, UUID id) {
        log.debug("Before delete {} [tenant='{}', id={}]", elementType, tenant, id);
    }

    @Override
    public void afterDelete(String tenant, UUID id) {
        log.debug("After delete {} [tenant='{}', id={}]", elementType, tenant, id);
    }

    @Override
    public void beforeDelete(String tenant, List<T> objects) {
        log.debug("Before delete {} batch [tenant='{}', {} items]", elementType, tenant, objects.size());
    }

    @Override
    public void afterDelete(String tenant, List<T> objects) {
        log.debug("After delete {} batch [tenant='{}', {} items]", elementType, tenant, objects.size());
    }

    @Override
    public T beforeCreate(String tenant, T object) {
        log.debug("Before create {} [tenant='{}', id={}]", elementType, tenant, object.getId());
        return object;
    }

    @Override
    public List<T> afterFindAll(String tenant, List<T> list) {
        log.debug("After find all {} [tenant='{}', {} items]", elementType, tenant, list.size());
        return list;
    }

    @Override
    public T afterFindById(String tenant, T object) {
        log.debug("After find by id {} [tenant='{}', id={}]", elementType, tenant, object.getId());
        return object;
    }

    @Override
    public T afterCreate(String tenant, T object) {
        log.debug("After create {} [tenant='{}', id={}]", elementType, tenant, object.getId());
        return object;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateTenant(String tenant) {
        if (!StringUtils.hasText(tenant)) {
            throw new InvalidTenantException("Tenant cannot be null or empty");
        }
    }

    private E findEntityById(String tenant, UUID id) {
        return repository()
                .findByElementTypeAndJsonIdAndTenant(elementType, id.toString(), tenant)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Entity not found for type='%s', id='%s', tenant='%s'"
                                .formatted(elementType, id, tenant)));
    }
}