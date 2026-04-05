package eu.isygoit.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.annotation.ElementType;
import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import jakarta.persistence.Table;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Static utility methods shared by the JSON-based service layer.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Resolving the element type key and physical table name from class metadata.</li>
 *   <li>Converting between {@link JsonBasedEntity} (JPA row) and {@link JsonElement} (POJO).</li>
 *   <li>Validating and evaluating criteria filters in-memory (for operators not pushed to the DB).</li>
 *   <li>Applying pagination to an already-filtered in-memory list.</li>
 * </ul>
 */
@Slf4j
public class JsonBasedEntityHelper {

    private JsonBasedEntityHelper() {
    }

    // ── Element type and table name resolution (point 6) ─────────────────────

    /**
     * Resolves the stable storage key for a {@link JsonElement} class.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>{@link ElementType#value()} if the annotation is present and non-blank.</li>
     *   <li>The simple class name in upper case (fallback — a {@code WARN} is logged
     *       to encourage explicit annotation and surface potential collisions early).</li>
     * </ol>
     *
     * @param cls the {@code JsonElement} implementation class
     * @param <T> type bound
     * @return the element type key, never {@code null} or blank
     */
    public static <T extends JsonElement<?>> String resolveElementType(Class<T> cls) {
        ElementType annotation = cls.getAnnotation(ElementType.class);
        if (annotation != null && StringUtils.hasText(annotation.value())) {
            return annotation.value().toUpperCase();
        }
        log.warn("Class '{}' has no @ElementType annotation. Falling back to simple class name '{}'. "
                        + "This may cause collisions if two classes share the same simple name across packages. "
                        + "Annotate the class with @ElementType(\"STABLE_KEY\") to suppress this warning.",
                cls.getName(), cls.getSimpleName().toUpperCase());
        return cls.getSimpleName().toUpperCase();
    }

    /**
     * Resolves the physical table name for a JPA entity class.
     *
     * <p>Uses {@link Table#name()} when the annotation is present and non-blank,
     * otherwise falls back to the simple class name in lower case.
     *
     * @param entityClass the JPA entity class
     * @param <E>         type bound
     * @return the physical table name, never {@code null} or blank
     */
    public static <E> String resolveTableName(Class<E> entityClass) {
        Table tableAnnotation = entityClass.getAnnotation(Table.class);
        if (tableAnnotation != null && StringUtils.hasText(tableAnnotation.name())) {
            return tableAnnotation.name().toLowerCase();
        }
        log.warn("Entity '{}' has no @Table(name=...) annotation. Falling back to simple class name '{}'. "
                        + "Ensure this matches the actual table name in your schema.",
                entityClass.getName(), entityClass.getSimpleName().toLowerCase());
        return entityClass.getSimpleName().toLowerCase();
    }

    // ── Entity ↔ element conversion ───────────────────────────────────────────

    /**
     * Converts a {@link JsonElement} POJO into a new {@link JsonBasedEntity} instance,
     * serialising the POJO as the {@code attributes} JSONB column value.
     *
     * @param element         the POJO to persist
     * @param elementType     the discriminator key written to {@code element_type}
     * @param jsonEntityClass the concrete entity class to instantiate
     * @param objectMapper    Jackson mapper used for serialisation
     * @param <T>             JsonElement type
     * @param <E>             JsonBasedEntity type
     * @return a populated, unpersisted entity instance
     */
    public static <T extends IIdAssignable<UUID> & JsonElement<UUID>,
            E extends JsonBasedEntity<?> & IIdAssignable<?>>
    E toJsonEntity(T element, String elementType, Class<E> jsonEntityClass, ObjectMapper objectMapper) {
        var json = objectMapper.valueToTree(element);
        try {
            var jsonEntity = jsonEntityClass.getDeclaredConstructor().newInstance();
            jsonEntity.setElementType(elementType);
            jsonEntity.setAttributes(json);
            return jsonEntity;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to instantiate entity class '%s' for elementType '%s'."
                            .formatted(jsonEntityClass.getSimpleName(), elementType), e);
        }
    }

    /**
     * Converts a {@link JsonBasedEntity} row back into a {@link JsonElement} POJO
     * by deserialising the {@code attributes} JSONB column.
     *
     * @param jsonEntity       the entity row
     * @param jsonElementClass the target POJO class
     * @param objectMapper     Jackson mapper used for deserialisation
     * @param <T>              JsonElement type
     * @param <E>              JsonBasedEntity type
     * @return the deserialised POJO
     */
    public static <T extends IIdAssignable<UUID> & JsonElement<UUID>,
            E extends JsonBasedEntity<?>>
    T toJsonElement(E jsonEntity, Class<T> jsonElementClass, ObjectMapper objectMapper) {
        return objectMapper.convertValue(jsonEntity.getAttributes(), jsonElementClass);
    }

    // ── ID assignment ─────────────────────────────────────────────────────────

    /**
     * Assigns a random {@link UUID} to the object if its current ID is {@code null}.
     *
     * @param object the object whose ID to populate
     * @param <T>    type bound
     */
    public static <T extends IIdAssignable<UUID>> void assignIdIfNull(T object) {
        if (object.getId() == null) {
            object.setId(UUID.randomUUID());
        }
    }

    // ── In-memory criteria filtering ──────────────────────────────────────────

    /**
     * Validates that every criterion name matches a declared field of the given class.
     *
     * @param jsonElementClass the class whose fields define valid criterion names
     * @param criteria         the criteria to validate
     * @param <T>              type bound
     * @throws WrongCriteriaFilterException if any criterion names an undeclared field
     */
    public static <T> void validateCriteriaAgainstJsonElement(Class<T> jsonElementClass,
                                                              List<QueryCriteria> criteria) {
        var validFields = CriteriaHelper.getCriteriaData(jsonElementClass);
        for (QueryCriteria criterion : criteria) {
            if (!validFields.containsKey(criterion.getName())) {
                throw new WrongCriteriaFilterException("with name: " + criterion.getName());
            }
        }
    }

    /**
     * Filters a list of entities in memory using the provided criteria.
     * Intended for operators that cannot be pushed to the database (e.g. {@code BW}).
     *
     * @param entities    the full candidate list (already narrowed by DB query)
     * @param criteria    the criteria to evaluate in Java
     * @param elementType used only for debug logging
     * @param <T>         entity type
     * @return the filtered list
     */
    public static <T> List<T> applyCriteriaFilter(List<T> entities,
                                                  List<QueryCriteria> criteria,
                                                  String elementType) {
        if (criteria == null || criteria.isEmpty()) {
            log.warn("applyCriteriaFilter called with no criteria — returning all {} entities.", elementType);
            return entities;
        }
        List<T> filtered = entities.stream()
                .filter(entity -> evaluateCriteria(entity, criteria))
                .collect(Collectors.toList());
        log.debug("In-memory filter kept {}/{} '{}' entities.", filtered.size(), entities.size(), elementType);
        return filtered;
    }

    /**
     * Evaluates all criteria against a single entity instance using AND/OR combiners.
     *
     * @param entity   the candidate entity
     * @param criteria the criteria to evaluate
     * @param <T>      entity type
     * @return {@code true} if the entity satisfies the combined criteria
     */
    public static <T> boolean evaluateCriteria(T entity, List<QueryCriteria> criteria) {
        boolean result = true;
        for (int i = 0; i < criteria.size(); i++) {
            QueryCriteria criterion = criteria.get(i);
            boolean criterionResult = evaluateSingleCriterion(entity, criterion);
            if (i == 0) {
                result = criterionResult;
            } else if (criterion.getCombiner() == IEnumCriteriaCombiner.Types.AND) {
                result = result && criterionResult;
            } else {
                result = result || criterionResult;
            }
        }
        return result;
    }

    /**
     * Evaluates a single criterion against an entity field value.
     *
     * @param entity    the candidate entity
     * @param criterion the criterion to evaluate
     * @param <T>       entity type
     * @return {@code true} if the criterion is satisfied
     * @throws WrongCriteriaFilterException if the field cannot be accessed or the operator
     *                                      is unrecognised / receives an invalid value
     */
    public static <T> boolean evaluateSingleCriterion(T entity, QueryCriteria criterion) {
        try {
            Field field = entity.getClass().getDeclaredField(criterion.getName());
            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            String value = fieldValue != null ? fieldValue.toString() : null;
            String criterionValue = criterion.getValue();

            if (value == null
                    && criterion.getOperator() != IEnumOperator.Types.EQ
                    && criterion.getOperator() != IEnumOperator.Types.NE) {
                return false;
            }

            return switch (criterion.getOperator()) {
                case EQ -> criterionValue.equals(value);
                case NE -> value == null
                        ? !criterionValue.equals("null")
                        : !criterionValue.equals(value);
                case LI -> value != null && value.contains(criterionValue);
                case NL -> value == null || !value.contains(criterionValue);
                case GT -> compareDoubleOrString(value, criterionValue) > 0;
                case GE -> compareDoubleOrString(value, criterionValue) >= 0;
                case LT -> compareDoubleOrString(value, criterionValue) < 0;
                case LE -> compareDoubleOrString(value, criterionValue) <= 0;
                case BW -> evaluateBetween(value, criterionValue, criterion);
                default -> throw new WrongCriteriaFilterException(
                        "Unsupported operator: " + criterion.getOperator());
            };
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WrongCriteriaFilterException("Error accessing field: " + criterion.getName());
        }
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    /**
     * Applies page/size slicing to a pre-filtered in-memory list.
     * Equivalent to what the DB would do with {@code LIMIT}/{@code OFFSET}.
     *
     * @param entities    the full filtered list
     * @param pageRequest the desired page
     * @param <T>         entity type
     * @return the requested page slice, or an empty list if the offset exceeds the list size
     */
    public static <T> List<T> applyPagination(List<T> entities, PageRequest pageRequest) {
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), entities.size());
        return start < entities.size() ? entities.subList(start, end) : List.of();
    }

    // ── Private comparison helpers ────────────────────────────────────────────

    /**
     * Compares two values numerically if both parse as doubles, otherwise lexicographically.
     */
    private static int compareDoubleOrString(String value, String criterionValue) {
        try {
            return Double.compare(Double.parseDouble(value), Double.parseDouble(criterionValue));
        } catch (NumberFormatException e) {
            return value != null ? value.compareTo(criterionValue) : -1;
        }
    }

    /**
     * Evaluates the BETWEEN operator (format: {@code "min:max"}) against a field value.
     */
    private static boolean evaluateBetween(String value, String criterionValue,
                                           QueryCriteria criterion) {
        if (!criterionValue.contains(":")) {
            throw new WrongCriteriaFilterException(
                    "BETWEEN operator requires value in 'min:max' format, got: " + criterionValue);
        }
        String[] range = criterionValue.split(":", 2);
        if (range.length != 2 || !StringUtils.hasText(range[0]) || !StringUtils.hasText(range[1])) {
            throw new WrongCriteriaFilterException("Invalid BETWEEN value format: " + criterionValue);
        }
        try {
            double d = Double.parseDouble(value);
            double min = Double.parseDouble(range[0]);
            double max = Double.parseDouble(range[1]);
            return d >= min && d <= max;
        } catch (NumberFormatException e) {
            return value != null
                    && value.compareTo(range[0]) >= 0
                    && value.compareTo(range[1]) <= 0;
        }
    }
}