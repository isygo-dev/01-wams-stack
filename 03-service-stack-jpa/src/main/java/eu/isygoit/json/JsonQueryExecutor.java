package eu.isygoit.json;

import eu.isygoit.filter.QueryCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes dynamic JSONB-aware native queries against the JSON entity table,
 * delegating SQL construction to {@link eu.isygoit.json.JsonCriteriaQueryBuilder}.
 *
 * <h3>Why {@code EntityManager} and not a derived repository method?</h3>
 * Spring Data derived method names and {@code @Query} annotations are static.
 * Criteria filtering requires a variable number of predicates with variable
 * operators, which can only be expressed as a dynamically assembled SQL string.
 * {@code EntityManager.createNativeQuery} is the standard JPA entry point for
 * this pattern.
 *
 * <h3>SQL injection risk</h3>
 * The table name and element type are framework-internal constants derived from
 * JPA annotations and class metadata — never from user input. JSONB field names
 * come from criteria validated against declared Java fields by
 * {@link eu.isygoit.helper.JsonBasedEntityHelper#validateCriteriaAgainstJsonElement}.
 * All user-supplied filter values are bound as named parameters, never interpolated.
 *
 * <h3>Result mapping</h3>
 * The query is executed with an entity class so Hibernate maps each row to a
 * managed entity instance. {@code @JdbcTypeCode(SqlTypes.JSON)} columns (the
 * {@code attributes} JSONB column) are handled transparently by Hibernate 6+.
 */
@Slf4j
@Component
public class JsonQueryExecutor {

    @PersistenceContext
    private EntityManager em;

    /**
     * Runs a filtered native query against the JSON entity table and returns the
     * matched rows as JPA entity instances of type {@code E}.
     *
     * <p>The generated SQL follows this shape:
     * <pre>
     *   SELECT * FROM {tableName} e
     *   WHERE e.element_type = :elementType
     *   [AND e.tenant_id = :tenant]
     *   [AND|OR &lt;jsonb predicates from dbCriteria&gt;]
     * </pre>
     *
     * @param tableName   physical table name (e.g. {@code "events"})
     * @param elementType the element type discriminator stored in each row
     * @param tenant      tenant value, or {@code null}/{@code ""} for non-tenant queries
     * @param dbCriteria  criteria already confirmed as DB-pushable by
     *                    {@link JsonCriteriaQueryBuilder#partition}; may be empty
     * @param entityClass the JPA entity class for Hibernate result mapping
     * @param <E>         the entity type
     * @return matched rows, each mapped to an instance of {@code entityClass}
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public <E> List<E> findByCriteria(
            String tableName,
            String elementType,
            String tenant,
            List<QueryCriteria> dbCriteria,
            Class<E> entityClass) {

        Map<String, Object> params = new LinkedHashMap<>();
        params.put("elementType", elementType);

        StringBuilder sql = new StringBuilder(
                "SELECT * FROM %s e WHERE e.element_type = :elementType".formatted(tableName));

        if (StringUtils.hasText(tenant)) {
            params.put("tenant", tenant);
            sql.append(" AND e.tenant_id = :tenant");
        }

        // Append dynamic JSONB predicates, if any
        if (dbCriteria != null && !dbCriteria.isEmpty()) {
            sql.append(JsonCriteriaQueryBuilder.buildWhereFragment(dbCriteria, params));
        }

        log.debug("Executing JSON criteria query for elementType='{}': {}", elementType, sql);

        var query = em.createNativeQuery(sql.toString(), entityClass);
        params.forEach(query::setParameter);

        return query.getResultList();
    }
}