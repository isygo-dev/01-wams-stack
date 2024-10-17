package eu.isygoit.helper;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.enums.IEnumCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.model.IIdEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

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
    public static List<eu.isygoit.filter.Criteria> convertStringToCriteria(String criteria, String delim) {
        List<eu.isygoit.filter.Criteria> list = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(criteria, delim);

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            //get combiner if exists
            String[] combinerKeyValue = token.split("->");
            IEnumCombiner.Types combiner = IEnumCombiner.Types.OR;
            if (combinerKeyValue.length == 2) {
                combiner = IEnumCombiner.Types.valueOf(combinerKeyValue[0].trim());
                token = combinerKeyValue[1];
            }


            String finalToken = token;
            IEnumCombiner.Types finalCombiner = combiner;
            Arrays.stream(IEnumOperator.Types.values()).forEach(operator -> {
                if (finalToken.contains(operator.symbol())) {
                    String[] keyValue = finalToken.split(operator.symbol());
                    list.add(eu.isygoit.filter.Criteria.builder()
                            .combiner(finalCombiner)
                            .name(keyValue[0])
                            .operator(operator)
                            .value(keyValue[1])
                            .build());
                    return;
                }
            });
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
        Arrays.stream(classType.getDeclaredFields()).forEach(field -> {
            Criteria criteria = field.getAnnotation(Criteria.class);
            if (criteria != null) {
                criteriaMap.put(field.getName(), field.getType().getSimpleName());
            }
        });
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
    public static <T extends IIdEntity> Specification<T> buildSpecification(String domain, List<eu.isygoit.filter.Criteria> criteria, Class<?> classType) {
        Map<String, String> criteriaMap = CriteriaHelper.getCriteriaData(classType);
        Specification<T> specification = Specification.where(null);
        for (eu.isygoit.filter.Criteria cr : criteria) {
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
    public static <T extends IIdEntity> Specification<T> like(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> notLike(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> equal(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> notEqual(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> lessThan(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> lessThanOrEqualTo(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> greaterThan(String valueName, String value) {
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
    public static <T extends IIdEntity> Specification<T> greaterThanOrEqualTo(String valueName, String value) {
        return (entity, cq, cb) -> cb.greaterThanOrEqualTo(entity.get(valueName), value);
    }
}
