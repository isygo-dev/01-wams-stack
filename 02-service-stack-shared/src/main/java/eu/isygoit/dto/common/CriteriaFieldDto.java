package eu.isygoit.dto.common;

/**
 * One field of the annotated-criteria metadata returned by
 * {@code GET /filter/criteria}.
 *
 * <p>Returned as an ordered {@link java.util.List} so the {@code @Criteria.order()}
 * ordering established on the backend (see {@code CriteriaHelper.getCriteriaData})
 * reaches the frontend intact. A {@code List} is the only collection whose order
 * is guaranteed both by the wire format (a JSON array) and by every implementation
 * of {@code List} — unlike {@code Map}, whose iteration order is implementation-defined
 * and, on the serialization side, can be overridden by
 * {@code SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS}.
 *
 * @param name the criteria field name (matches an entity field annotated with {@code @Criteria})
 * @param type the field's type simple name (e.g. {@code "String"}, {@code "LocalDate"}, {@code "Types"})
 */
public record CriteriaFieldDto(String name, String type) {
}