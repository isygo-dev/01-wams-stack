package eu.isygoit.jpa.json;

import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import eu.isygoit.model.ITenantAssignable;
import eu.isygoit.model.json.JsonBasedEntity;
import eu.isygoit.model.json.JsonElement;
import eu.isygoit.repository.json.JsonBasedTenantAssignableRepository;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class JsonBasedCriteriaHelper {

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

    public static <T> boolean evaluateSingleCriterion(T entity, QueryCriteria criterion) {
        try {
            // Access the field value using reflection
            Field field = entity.getClass().getDeclaredField(criterion.getName());
            field.setAccessible(true);
            Object fieldValue = field.get(entity);
            String value = fieldValue != null ? fieldValue.toString() : null;
            String criterionValue = criterion.getValue();

            // Handle null cases
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
}