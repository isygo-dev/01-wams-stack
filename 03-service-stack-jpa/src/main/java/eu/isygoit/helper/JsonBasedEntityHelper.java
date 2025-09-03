package eu.isygoit.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The type Json based entity helper.
 */
@Slf4j
public class JsonBasedEntityHelper {

    /**
     * Evaluates a list of criteria against an entity.
     *
     * @param <T>      the type parameter
     * @param entity   the entity
     * @param criteria the criteria
     * @return the boolean
     */
    public static <T> boolean evaluateCriteria(T entity, List<QueryCriteria> criteria) {
        boolean result = true; // Default for first criterion
        for (int i = 0; i < criteria.size(); i++) {
            QueryCriteria criterion = criteria.get(i);
            boolean criterionResult = evaluateSingleCriterion(entity, criterion);
            IEnumCriteriaCombiner.Types combiner = criterion.getCombiner();

            if (i == 0) {
                result = criterionResult;
            } else {
                if (combiner == IEnumCriteriaCombiner.Types.AND) {
                    result = result && criterionResult;
                } else { // OR
                    result = result || criterionResult;
                }
            }
        }
        return result;
    }

    /**
     * Evaluates a single criterion against an entity.
     *
     * @param <T>       the type parameter
     * @param entity    the entity
     * @param criterion the criterion
     * @return the boolean
     */
    public static <T> boolean evaluateSingleCriterion(T entity, QueryCriteria criterion) {
        try {
            Field field = entity.getClass().getDeclaredField(criterion.getName());
            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            String value = fieldValue != null ? fieldValue.toString() : null;
            String criterionValue = criterion.getValue();

            if (value == null && criterion.getOperator() != IEnumOperator.Types.EQ && criterion.getOperator() != IEnumOperator.Types.NE) {
                return false;
            }

            switch (criterion.getOperator()) {
                case EQ:
                    return criterionValue.equals(value);
                case NE:
                    return value == null ? !criterionValue.equals("null") : !criterionValue.equals(value);
                case LI:
                    return value != null && value.contains(criterionValue);
                case NL:
                    return value == null || !value.contains(criterionValue);
                case GT:
                    try {
                        double doubleValue = Double.parseDouble(value);
                        double criterionDouble = Double.parseDouble(criterionValue);
                        return doubleValue > criterionDouble;
                    } catch (NumberFormatException e) {
                        return value != null && value.compareTo(criterionValue) > 0;
                    }
                case GE:
                    try {
                        double doubleValue = Double.parseDouble(value);
                        double criterionDouble = Double.parseDouble(criterionValue);
                        return doubleValue >= criterionDouble;
                    } catch (NumberFormatException e) {
                        return value != null && value.compareTo(criterionValue) >= 0;
                    }
                case LT:
                    try {
                        double doubleValue = Double.parseDouble(value);
                        double criterionDouble = Double.parseDouble(criterionValue);
                        return doubleValue < criterionDouble;
                    } catch (NumberFormatException e) {
                        return value != null && value.compareTo(criterionValue) < 0;
                    }
                case LE:
                    try {
                        double doubleValue = Double.parseDouble(value);
                        double criterionDouble = Double.parseDouble(criterionValue);
                        return doubleValue <= criterionDouble;
                    } catch (NumberFormatException e) {
                        return value != null && value.compareTo(criterionValue) <= 0;
                    }
                case BW:
                    if (!criterionValue.contains(":")) {
                        throw new WrongCriteriaFilterException("BETWEEN operator requires value in format 'min:max', got: " + criterionValue);
                    }
                    String[] range = criterionValue.split(":", 2);
                    if (range.length != 2 || !StringUtils.hasText(range[0]) || !StringUtils.hasText(range[1])) {
                        throw new WrongCriteriaFilterException("Invalid BETWEEN value format: " + criterionValue);
                    }
                    try {
                        double doubleValue = Double.parseDouble(value);
                        double min = Double.parseDouble(range[0]);
                        double max = Double.parseDouble(range[1]);
                        return doubleValue >= min && doubleValue <= max;
                    } catch (NumberFormatException e) {
                        return value != null && value.compareTo(range[0]) >= 0 && value.compareTo(range[1]) <= 0;
                    }
                default:
                    throw new WrongCriteriaFilterException("Unsupported operator: " + criterion.getOperator());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new WrongCriteriaFilterException("Error accessing field: " + criterion.getName());
        }
    }

    /**
     * Validates criteria against the fields of a given JSON element class.
     *
     * @param <T>              the type parameter
     * @param jsonElementClass the json element class
     * @param criteria         the criteria
     */
    public static <T> void validateCriteriaAgainstJsonElement(Class<T> jsonElementClass, List<QueryCriteria> criteria) {
        var validFields = CriteriaHelper.getCriteriaData(jsonElementClass);
        for (QueryCriteria criterion : criteria) {
            if (!validFields.containsKey(criterion.getName())) {
                throw new WrongCriteriaFilterException("with name: " + criterion.getName());
            }
        }
    }

    /**
     * Assigns a random UUID to the object if its ID is null.
     *
     * @param <T>    the type parameter
     * @param object the object
     */
    public static <T extends IIdAssignable<UUID>> void assignIdIfNull(T object) {
        if (object.getId() == null) {
            object.setId(UUID.randomUUID());
        }
    }

    /**
     * Converts a JSON element to a JSON entity.
     *
     * @param <T>             the type parameter
     * @param <E>             the type parameter
     * @param element         the element
     * @param elementType     the element type
     * @param jsonEntityClass the json entity class
     * @param objectMapper    the object mapper
     * @return the e
     */
    public static <T extends IIdAssignable<UUID> & JsonElement<UUID>, E extends JsonBasedEntity<?> & IIdAssignable<?>>
    E toJsonEntity(T element, String elementType, Class<E> jsonEntityClass, ObjectMapper objectMapper) {
        var json = objectMapper.valueToTree(element);
        try {
            var jsonEntity = jsonEntityClass.getDeclaredConstructor().newInstance();
            jsonEntity.setElementType(elementType);
            jsonEntity.setAttributes(json);
            return jsonEntity;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create %s instance" .formatted(elementType), e);
        }
    }

    /**
     * Converts a JSON entity to a JSON element.
     *
     * @param <T>              the type parameter
     * @param <E>              the type parameter
     * @param jsonEntity       the json entity
     * @param jsonElementClass the json element class
     * @param objectMapper     the object mapper
     * @return the t
     */
    public static <T extends IIdAssignable<UUID> & JsonElement<UUID>, E extends JsonBasedEntity<?>>
    T toJsonElement(E jsonEntity, Class<T> jsonElementClass, ObjectMapper objectMapper) {
        return objectMapper.convertValue(jsonEntity.getAttributes(), jsonElementClass);
    }

    /**
     * Applies pagination to a list of entities.
     *
     * @param <T>         the type parameter
     * @param entities    the entities
     * @param pageRequest the page request
     * @return the list
     */
    public static <T> List<T> applyPagination(List<T> entities, PageRequest pageRequest) {
        int start = (int) pageRequest.getOffset();
        int end = Math.min(start + pageRequest.getPageSize(), entities.size());
        return start < entities.size() ? entities.subList(start, end) : List.of();
    }

    /**
     * Applies criteria filtering to a list of entities.
     *
     * @param <T>         the type parameter
     * @param entities    the entities
     * @param criteria    the criteria
     * @param elementType the element type
     * @return the list
     */
    public static <T> List<T> applyCriteriaFilter(List<T> entities, List<QueryCriteria> criteria, String elementType) {
        if (criteria == null || criteria.isEmpty()) {
            log.warn("No criteria provided, returning all entities");
            return entities;
        }
        List<T> filtered = entities.stream()
                .filter(entity -> evaluateCriteria(entity, criteria))
                .collect(Collectors.toList());
        log.debug("Found {} {} entities", filtered.size(), elementType);
        return filtered;
    }
}