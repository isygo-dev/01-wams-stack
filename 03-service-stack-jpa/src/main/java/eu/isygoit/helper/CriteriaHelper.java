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
     * @param criteria the criteria
     * @param delim    the delim
     * @return the list
     */
    public static List<QueryCriteria> convertStringToCriteria(String criteria, String delim) {
        List<QueryCriteria> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(criteria, delim);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();

            // Get combiner if exists
            String[] combinerKeyValue = token.split("->", 2); // Limit split to avoid unnecessary split on the value side
            IEnumCriteriaCombiner.Types combiner = IEnumCriteriaCombiner.Types.OR;

            if (combinerKeyValue.length == 2) {
                combiner = IEnumCriteriaCombiner.Types.valueOf(combinerKeyValue[0].trim());
                token = combinerKeyValue[1].trim();
            }

            // Loop through operators and process the token
            for (IEnumOperator.Types operator : IEnumOperator.Types.values()) {
                if (token.contains(operator.symbol())) {
                    String[] keyValue = token.split(operator.symbol(), 2); // Split only once
                    if (keyValue.length == 2) {
                        list.add(QueryCriteria.builder()
                                .combiner(combiner)
                                .name(keyValue[0].trim())
                                .operator(operator)
                                .value(keyValue[1].trim())
                                .build());
                    }
                    break; // No need to continue once a valid operator is found
                }
            }
        }

        return list;
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
     * @param domain    the domain
     * @param criteria  the criteria
     * @param classType the class type
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> buildSpecification(String domain, List<QueryCriteria> criteria, Class<?> classType) {
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
        if (StringUtils.hasText(domain)) {
            specification = specification.and(CriteriaHelper.equal("domain", domain));
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
