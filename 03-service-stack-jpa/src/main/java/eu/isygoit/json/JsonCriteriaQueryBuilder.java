package eu.isygoit.json;

import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.filter.QueryCriteria;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Translates a list of {@link QueryCriteria} into a parameterised PostgreSQL JSONB
 * WHERE clause fragment, appended to the base query built by {@link JsonQueryExecutor}.
 *
 * <h3>Design</h3>
 * <ul>
 *   <li>Operators that map cleanly to JSONB SQL ({@code EQ, NE, LI, NL, GT, GE, LT, LE})
 *       are pushed to the database. The DB filters the rows <em>before</em> any network
 *       or deserialization cost is paid.</li>
 *   <li>The {@code BW} (between) operator is left for in-memory evaluation via
 *       {@link eu.isygoit.helper.JsonBasedEntityHelper#applyCriteriaFilter} because it requires parsing
 *       a composite {@code "min:max"} string and handling mixed numeric / lexicographic
 *       comparisons — logic already well-tested in the helper.</li>
 *   <li>Callers should always run {@link #partition} first, pass the DB list to this
 *       builder, then apply the in-memory list to the returned results.</li>
 * </ul>
 *
 * <h3>SQL injection safety</h3>
 * Field names come from {@link QueryCriteria#getName()} which is validated upstream
 * by {@link eu.isygoit.helper.JsonBasedEntityHelper#validateCriteriaAgainstJsonElement} against the
 * declared fields of the {@code JsonElement} class. Values are always bound as
 * named parameters — never interpolated into the SQL string.
 */
@Slf4j
public final class JsonCriteriaQueryBuilder {

    /**
     * Operators whose semantics map directly to a PostgreSQL JSONB predicate.
     */
    public static final Set<IEnumOperator.Types> DB_PUSHABLE = Set.of(
            IEnumOperator.Types.EQ,
            IEnumOperator.Types.NE,
            IEnumOperator.Types.LI,
            IEnumOperator.Types.NL,
            IEnumOperator.Types.GT,
            IEnumOperator.Types.GE,
            IEnumOperator.Types.LT,
            IEnumOperator.Types.LE
    );

    private JsonCriteriaQueryBuilder() {
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Splits the criteria list into two groups:
     * <ol>
     *   <li>DB-pushable criteria (key) — safe to send to PostgreSQL as JSONB predicates.</li>
     *   <li>In-memory criteria (value) — must be evaluated in Java after the DB fetch.</li>
     * </ol>
     *
     * @param criteria the full criteria list
     * @return a {@link Map.Entry} where key = DB list, value = in-memory list
     */
    public static Map.Entry<List<QueryCriteria>, List<QueryCriteria>> partition(
            List<QueryCriteria> criteria) {

        List<QueryCriteria> dbPart = criteria.stream()
                .filter(c -> DB_PUSHABLE.contains(c.getOperator()))
                .toList();
        List<QueryCriteria> memPart = criteria.stream()
                .filter(c -> !DB_PUSHABLE.contains(c.getOperator()))
                .toList();

        if (!memPart.isEmpty()) {
            log.debug("{} criterion/criteria will be evaluated in-memory (operators: {})",
                    memPart.size(),
                    memPart.stream().map(c -> c.getOperator().name()).toList());
        }

        return Map.entry(dbPart, memPart);
    }

    /**
     * Builds the dynamic portion of a WHERE clause for the given DB-pushable criteria
     * and populates the {@code params} map with the corresponding named parameter values.
     *
     * <p>The caller is responsible for constructing the base query:
     * <pre>
     *   SELECT * FROM {table} e
     *   WHERE e.element_type = :elementType
     *   [AND e.tenant_id = :tenant]
     * </pre>
     * This method appends additional {@code AND}/{@code OR} predicates on top.
     *
     * <p>Parameter keys use the prefix {@code crit} followed by the criterion index
     * (e.g. {@code crit0}, {@code crit1}) to avoid collisions with the fixed parameters
     * {@code elementType} and {@code tenant}.
     *
     * @param criteria the DB-pushable criteria (must all have operators in {@link #DB_PUSHABLE})
     * @param params   the mutable parameter map to populate; must already contain the
     *                 base parameters ({@code elementType}, optionally {@code tenant})
     * @return a SQL fragment starting with {@code " AND "} or {@code " OR "}, or an empty
     * string when the criteria list is empty
     * @throws WrongCriteriaFilterException if a criterion uses a non-DB-pushable operator
     *                                      or if a numeric operator receives a non-numeric value
     */
    public static String buildWhereFragment(List<QueryCriteria> criteria,
                                            Map<String, Object> params) {
        if (criteria.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < criteria.size(); i++) {
            QueryCriteria c = criteria.get(i);
            String paramKey = "crit" + i;

            // The first criterion always AND-connects to the fixed base predicates.
            // Subsequent criteria respect their declared combiner.
            if (i == 0) {
                sb.append(" AND ");
            } else {
                sb.append(c.getCombiner() == IEnumCriteriaCombiner.Types.OR ? " OR " : " AND ");
            }

            sb.append(buildFragment(c, paramKey, params));
        }

        return sb.toString();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Produces a single JSONB SQL predicate for one criterion and records the
     * corresponding parameter in {@code params}.
     */
    private static String buildFragment(QueryCriteria c, String paramKey,
                                        Map<String, Object> params) {
        String field = c.getName();   // validated upstream — safe to interpolate
        String value = c.getValue();

        return switch (c.getOperator()) {

            case EQ -> {
                params.put(paramKey, value);
                yield "e.attributes ->> '%s' = :%s".formatted(field, paramKey);
            }

            case NE -> {
                params.put(paramKey, value);
                // NULL JSONB field would not match '!=' so we guard it explicitly
                yield "(e.attributes ->> '%s' IS NULL OR e.attributes ->> '%s' != :%s)"
                        .formatted(field, field, paramKey);
            }

            case LI -> {
                params.put(paramKey, "%" + value + "%");
                yield "e.attributes ->> '%s' LIKE :%s".formatted(field, paramKey);
            }

            case NL -> {
                params.put(paramKey, "%" + value + "%");
                yield "(e.attributes ->> '%s' IS NULL OR e.attributes ->> '%s' NOT LIKE :%s)"
                        .formatted(field, field, paramKey);
            }

            case GT -> {
                params.put(paramKey, parseNumeric(value, c));
                yield "(e.attributes ->> '%s')::numeric > :%s".formatted(field, paramKey);
            }

            case GE -> {
                params.put(paramKey, parseNumeric(value, c));
                yield "(e.attributes ->> '%s')::numeric >= :%s".formatted(field, paramKey);
            }

            case LT -> {
                params.put(paramKey, parseNumeric(value, c));
                yield "(e.attributes ->> '%s')::numeric < :%s".formatted(field, paramKey);
            }

            case LE -> {
                params.put(paramKey, parseNumeric(value, c));
                yield "(e.attributes ->> '%s')::numeric <= :%s".formatted(field, paramKey);
            }

            default -> throw new WrongCriteriaFilterException(
                    "Operator '%s' is not DB-pushable and must be handled in-memory."
                            .formatted(c.getOperator()));
        };
    }

    private static double parseNumeric(String value, QueryCriteria c) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new WrongCriteriaFilterException(
                    "Operator %s requires a numeric value but received '%s' for field '%s'."
                            .formatted(c.getOperator(), value, c.getName()));
        }
    }
}