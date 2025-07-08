package eu.isygoit.helper;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * The type Criteria helper.
 */
//Exmp : cr1 = val1, OR cr2 != val2, AND cr3 > val3, OR cr4 >= val4, AND cr5 ~ val5
@Slf4j
public class CriteriaHelper {

    /**
     * Convert string to criteria list.
     *
     * @param sqlWhere the criteria
     * @return the list
     */
    public static List<QueryCriteria> convertsqlWhereToCriteria(String sqlWhere) {
        /*
            example
            sqlWhere = "ip = '152.2.3.236' & device = 'DEV' | (broken = true & canceled = false)"
         */
        List<QueryCriteria> criteriaList = new ArrayList<>();
        IEnumCriteriaCombiner.Types currentCombiner = IEnumCriteriaCombiner.Types.OR; // Default combiner

        // Remove WHERE keyword if present and trim
        sqlWhere = sqlWhere.replaceAll("(?i)^\\s*WHERE\\s*", "").trim();

        // Split on combiners (both & and | symbols)
        String[] conditionGroups = sqlWhere.split("\\s*([&|])\\s*");
        String[] combiners = sqlWhere.split("[^&|]+"); // Extract just the combiner symbols

        for (int i =  0; i < conditionGroups.length; i++) {
            String condition = conditionGroups[i].trim();
            if (i > 0 && combiners.length > i) {
                // Set combiner from the symbol (skip first element which is empty)
                currentCombiner = IEnumCriteriaCombiner.Types.valueOf(
                        combiners[i].trim().equals("&") ? "AND" : "OR"
                );
            }

            // Process each condition
            for (IEnumOperator.Types operator : IEnumOperator.Types.values()) {
                if (condition.contains(operator.symbol().trim())) {
                    String[] parts = condition.split(operator.symbol().trim(), 2);
                    if (parts.length == 2) {
                        String field = parts[0].trim();
                        String value = parts[1].trim();

                        // Remove surrounding quotes
                        if ((value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        criteriaList.add(QueryCriteria.builder()
                                .combiner(currentCombiner)
                                .name(field)
                                .operator(operator)
                                .value(value)
                                .build());
                    }
                    break;
                }
            }
        }

        return criteriaList;
    }

    /**
     * Gets criteria data.
     *
     * @param classType the class type
     * @return the criteria data
     */
    public static Map<String, String> getCriteriaData(Class<?> classType) {
        Map<String, String> criteriaMap = new HashMap<>();
        for (Field field : classType.getDeclaredFields()) {
            Criteria criteria = field.getAnnotation(Criteria.class);
            if (criteria != null) {
                criteriaMap.put(field.getName(), field.getType().getSimpleName());
            }
        }
        return criteriaMap;
    }

    /**
     * Build specification specification.
     *
     * @param <T>       the type parameter
     * @param tenant    the tenant
     * @param criteria  the criteria
     * @param classType the class type
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> buildSpecification(String tenant, List<QueryCriteria> criteria, Class<?> classType) {
        Map<String, String> criteriaMap = CriteriaHelper.getCriteriaData(classType);
        Specification<T> specification = Specification.where(null);
        for (QueryCriteria cr : criteria) {
            String name = cr.getName();
            String value = cr.getValue();
            if (!criteriaMap.containsKey(name)) {
                throw new WrongCriteriaFilterException("with name: " + name);
            }

            Specification<T> criteriaSpec;
            switch (cr.getOperator()) {
                case EQ:
                    criteriaSpec = CriteriaHelper.equal(name, value);
                    break;
                case NE:
                    criteriaSpec = CriteriaHelper.notEqual(name, value);
                    break;
                case LI:
                    criteriaSpec = CriteriaHelper.like(name, value);
                    break;
                case NL:
                    criteriaSpec = CriteriaHelper.notLike(name, value);
                    break;
                case LT:
                    criteriaSpec = CriteriaHelper.lessThan(name, value);
                    break;
                case LE:
                    criteriaSpec = CriteriaHelper.lessThanOrEqualTo(name, value);
                    break;
                case GT:
                    criteriaSpec = CriteriaHelper.greaterThan(name, value);
                    break;
                case GE:
                    criteriaSpec = CriteriaHelper.greaterThanOrEqualTo(name, value);
                    break;
                default:
                    throw new WrongCriteriaFilterException("with name: " + name);
            }

            switch (cr.getCombiner()) {
                case OR:
                    specification = specification.or(criteriaSpec);
                    break;
                case AND:
                    specification = specification.and(criteriaSpec);
                    break;
                default:
                    specification = specification.or(criteriaSpec);
            }
        }
        if (StringUtils.hasText(tenant)) {
            specification = specification.and(CriteriaHelper.equal("tenant", tenant));
        }
        return specification;
    }

    /**
     * Like specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> like(String valueName, String value) {
        return (entity, cq, cb) -> cb.like(entity.get(valueName), "%" + value + "%");
    }

    /**
     * Not like specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> notLike(String valueName, String value) {
        return (entity, cq, cb) -> cb.notLike(entity.get(valueName), "%" + value + "%");
    }

    /**
     * Equal specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> equal(String valueName, String value) {
        return (entity, cq, cb) -> cb.equal(entity.get(valueName), value);
    }

    /**
     * Not equal specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> notEqual(String valueName, String value) {
        return (entity, cq, cb) -> cb.notEqual(entity.get(valueName), value);
    }

    /**
     * Less than specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> lessThan(String valueName, String value) {
        return (entity, cq, cb) -> cb.lessThan(entity.get(valueName), value);
    }

    /**
     * Less than or equal to specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> lessThanOrEqualTo(String valueName, String value) {
        return (entity, cq, cb) -> cb.lessThanOrEqualTo(entity.get(valueName), value);
    }

    /**
     * Greater than specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> greaterThan(String valueName, String value) {
        return (entity, cq, cb) -> cb.greaterThan(entity.get(valueName), value);
    }

    /**
     * Greater than or equal to specification.
     *
     * @param <T>       the type parameter
     * @param valueName the value name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> greaterThanOrEqualTo(String valueName, String value) {
        return (entity, cq, cb) -> cb.greaterThanOrEqualTo(entity.get(valueName), value);
    }
}
