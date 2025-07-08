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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern CONDITION_PATTERN = Pattern.compile(
            "([\\w.]+)\\s*([=<>!~]+)\\s*('[^']*'|\"[^\"]*\"|\\S+)"
    );
    private static final Pattern LOGICAL_OP_PATTERN = Pattern.compile(
            "\\s*([&|])\\s*"
    );

    public static List<QueryCriteria> convertsqlWhereToCriteria(String sqlWhere) {
        List<QueryCriteria> criteriaList = new ArrayList<>();
        if (sqlWhere == null || sqlWhere.trim().isEmpty()) {
            return criteriaList;
        }

        // Remove WHERE keyword if present and trim
        sqlWhere = sqlWhere.replaceAll("(?i)^\\s*WHERE\\s*", "").trim();

        // Split into tokens while handling parentheses and logical operators
        List<String> tokens = new ArrayList<>();
        int start = 0;
        int parenLevel = 0;

        for (int i = 0; i < sqlWhere.length(); i++) {
            char c = sqlWhere.charAt(i);
            if (c == '(') {
                if (parenLevel == 0 && i > start) {
                    tokens.add(sqlWhere.substring(start, i).trim());
                    start = i;
                }
                parenLevel++;
            } else if (c == ')') {
                parenLevel--;
                if (parenLevel == 0) {
                    tokens.add(sqlWhere.substring(start, i + 1).trim());
                    start = i + 1;
                }
            } else if (parenLevel == 0 && (c == '&' || c == '|')) {
                if (i > start) {
                    tokens.add(sqlWhere.substring(start, i).trim());
                }
                tokens.add(String.valueOf(c));
                start = i + 1;
            }
        }

        if (start < sqlWhere.length()) {
            tokens.add(sqlWhere.substring(start).trim());
        }

        IEnumCriteriaCombiner.Types currentCombiner = IEnumCriteriaCombiner.Types.OR;

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);

            if (token.equals("&")) {
                currentCombiner = IEnumCriteriaCombiner.Types.AND;
                continue;
            } else if (token.equals("|")) {
                currentCombiner = IEnumCriteriaCombiner.Types.OR;
                continue;
            }

            if (token.startsWith("(") && token.endsWith(")")) {
                String nested = token.substring(1, token.length() - 1).trim();
                List<QueryCriteria> nestedCriteria = convertsqlWhereToCriteria(nested);
                if (!nestedCriteria.isEmpty()) {
                    // Set the combiner for the first nested condition
                    nestedCriteria.get(0).setCombiner(currentCombiner);
                    criteriaList.addAll(nestedCriteria);
                    // Reset combiner to OR after processing nested group
                    currentCombiner = IEnumCriteriaCombiner.Types.OR;
                }
                continue;
            }

            Matcher matcher = CONDITION_PATTERN.matcher(token);
            if (matcher.find()) {
                String field = matcher.group(1).trim();
                String operator = matcher.group(2).trim();
                String value = matcher.group(3).trim();

                // Remove surrounding quotes if present
                if ((value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                } else if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }

                IEnumOperator.Types operatorType = parseOperator(operator);
                criteriaList.add(QueryCriteria.builder()
                        .combiner(currentCombiner)
                        .name(field)
                        .operator(operatorType)
                        .value(value)
                        .build());

                // Reset combiner to OR after each condition unless overridden
                currentCombiner = IEnumCriteriaCombiner.Types.OR;
            }
        }

        return criteriaList;
    }

    private static IEnumOperator.Types parseOperator(String operator) {
        switch (operator) {
            case "=": return IEnumOperator.Types.EQ;
            case "!=": return IEnumOperator.Types.NE;
            case "~": return IEnumOperator.Types.LI;
            case "!~": return IEnumOperator.Types.NL;
            case "<": return IEnumOperator.Types.LT;
            case "<=": return IEnumOperator.Types.LE;
            case ">": return IEnumOperator.Types.GT;
            case ">=": return IEnumOperator.Types.GE;
            default: return IEnumOperator.Types.EQ;
        }
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
