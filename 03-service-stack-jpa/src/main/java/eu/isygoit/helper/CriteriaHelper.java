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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Enhanced criteria helper for building dynamic JPA specifications from SQL-like WHERE clauses.
 * Supports complex conditions with logical operators and parentheses grouping.
 * <p>
 * Example usage: "name = 'John' & age > 25 | (status = 'active' & role != 'admin')"
 */
@Slf4j
public final class CriteriaHelper {

    private static final Pattern CONDITION_PATTERN = Pattern.compile(
            "([\\w.]+)\\s*([=<>!~]+)\\s*('[^']*'|\"[^\"]*\"|\\S+)"
    );

    private static final Pattern LOGICAL_OP_PATTERN = Pattern.compile("\\s*([&|])\\s*");

    // Cache for criteria metadata to avoid repeated reflection
    private static final Map<Class<?>, Map<String, FieldInfo>> CRITERIA_CACHE = new ConcurrentHashMap<>();

    private static final Set<String> SUPPORTED_OPERATORS = Set.of("=", "!=", "~", "!~", "<", "<=", ">", ">=");

    // Private constructor to prevent instantiation
    private CriteriaHelper() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts SQL WHERE clause to criteria list with enhanced error handling and validation.
     *
     * @param sqlWhere the SQL WHERE clause string
     * @return list of query criteria
     * @throws IllegalArgumentException if the SQL WHERE clause is malformed
     */
    public static List<QueryCriteria> convertSqlWhereToCriteria(String sqlWhere) {
        if (sqlWhere == null || sqlWhere.isBlank()) {
            return List.of();
        }

        try {
            return parseWhereClause(normalizeWhereClause(sqlWhere));
        } catch (Exception e) {
            log.error("Failed to parse WHERE clause: {}", sqlWhere, e);
            throw new IllegalArgumentException("Invalid WHERE clause: " + sqlWhere, e);
        }
    }

    /**
     * Normalizes the WHERE clause by removing the WHERE keyword and trimming whitespace.
     */
    private static String normalizeWhereClause(String sqlWhere) {
        return sqlWhere.replaceAll("(?i)^\\s*WHERE\\s*", "").trim();
    }

    /**
     * Has balanced parentheses boolean.
     *
     * @param whereClause the where clause
     * @return the boolean
     */
    public static boolean hasBalancedParentheses(String whereClause) {
        long openCount = whereClause.chars().filter(c -> c == '(').count();
        long closeCount = whereClause.chars().filter(c -> c == ')').count();
        return openCount == closeCount;
    }

    /**
     * Parses the normalized WHERE clause into tokens and converts them to QueryCriteria.
     */
    private static List<QueryCriteria> parseWhereClause(String whereClause) {
        // Validate parentheses balance
        if (!hasBalancedParentheses(whereClause)) {
            throw new IllegalArgumentException("Unbalanced parentheses in WHERE clause: " + whereClause);
        }

        var tokens = tokenizeWhereClause(whereClause);
        var criteriaList = new ArrayList<QueryCriteria>();
        var currentCombiner = IEnumCriteriaCombiner.Types.OR;

        for (var token : tokens) {
            switch (token) {
                case "&" -> {
                    currentCombiner = IEnumCriteriaCombiner.Types.AND;
                    continue;
                }
                case "|" -> {
                    currentCombiner = IEnumCriteriaCombiner.Types.OR;
                    continue;
                }
                default -> {
                    var criteria = processToken(token, currentCombiner);
                    criteriaList.addAll(criteria);
                    currentCombiner = IEnumCriteriaCombiner.Types.OR;
                }
            }
        }

        return criteriaList;
    }

    /**
     * Tokenizes the WHERE clause while handling parentheses and logical operators.
     */
    private static List<String> tokenizeWhereClause(String whereClause) {
        var tokens = new ArrayList<String>();
        var start = 0;
        var parenLevel = 0;

        for (var i = 0; i < whereClause.length(); i++) {
            var c = whereClause.charAt(i);

            switch (c) {
                case '(' -> {
                    if (parenLevel == 0 && i > start) {
                        tokens.add(whereClause.substring(start, i).trim());
                        start = i;
                    }
                    parenLevel++;
                }
                case ')' -> {
                    parenLevel--;
                    if (parenLevel == 0) {
                        tokens.add(whereClause.substring(start, i + 1).trim());
                        start = i + 1;
                    }
                }
                case '&', '|' -> {
                    if (parenLevel == 0) {
                        if (i > start) {
                            tokens.add(whereClause.substring(start, i).trim());
                        }
                        tokens.add(String.valueOf(c));
                        start = i + 1;
                    }
                }
            }
        }

        if (start < whereClause.length()) {
            tokens.add(whereClause.substring(start).trim());
        }

        return tokens.stream()
                .filter(token -> !token.isBlank())
                .toList();
    }

    /**
     * Processes a single token (condition or nested group) into QueryCriteria.
     */
    private static List<QueryCriteria> processToken(String token, IEnumCriteriaCombiner.Types combiner) {
        if (token.startsWith("(") && token.endsWith(")")) {
            return processNestedCondition(token, combiner);
        }

        return List.of(parseCondition(token, combiner));
    }

    /**
     * Processes nested conditions within parentheses.
     */
    private static List<QueryCriteria> processNestedCondition(String token, IEnumCriteriaCombiner.Types combiner) {
        var nested = token.substring(1, token.length() - 1).trim();
        var nestedCriteria = convertSqlWhereToCriteria(nested);

        if (!nestedCriteria.isEmpty()) {
            nestedCriteria.get(0).setCombiner(combiner);
        }

        return nestedCriteria;
    }

    /**
     * Parses a single condition into QueryCriteria with enhanced validation.
     */
    private static QueryCriteria parseCondition(String condition, IEnumCriteriaCombiner.Types combiner) {
        var matcher = CONDITION_PATTERN.matcher(condition);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }

        var field = matcher.group(1).trim();
        var operator = matcher.group(2).trim();
        var value = normalizeValue(matcher.group(3).trim());

        validateOperator(operator);

        return QueryCriteria.builder()
                .combiner(combiner)
                .name(field)
                .operator(parseOperator(operator))
                .value(value)
                .build();
    }

    /**
     * Normalizes values by removing surrounding quotes.
     */
    private static String normalizeValue(String value) {
        if (value.length() >= 2) {
            var first = value.charAt(0);
            var last = value.charAt(value.length() - 1);

            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    /**
     * Validates that the operator is supported.
     */
    private static void validateOperator(String operator) {
        if (!SUPPORTED_OPERATORS.contains(operator)) {
            throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

    /**
     * Enhanced operator parsing with better error handling.
     */
    private static IEnumOperator.Types parseOperator(String operator) {
        return switch (operator) {
            case "=" -> IEnumOperator.Types.EQ;
            case "!=" -> IEnumOperator.Types.NE;
            case "~" -> IEnumOperator.Types.LI;
            case "!~" -> IEnumOperator.Types.NL;
            case "<" -> IEnumOperator.Types.LT;
            case "<=" -> IEnumOperator.Types.LE;
            case ">" -> IEnumOperator.Types.GT;
            case ">=" -> IEnumOperator.Types.GE;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };
    }

    /**
     * Gets criteria metadata with caching for better performance.
     *
     * @param classType the class type to analyze
     * @return map of field names to their type information
     */
    public static Map<String, String> getCriteriaData(Class<?> classType) {
        return getCriteriaFieldInfo(classType).entrySet().stream()
                .collect(HashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue().typeSimpleName()),
                        HashMap::putAll);
    }

    /**
     * Gets cached criteria field information.
     */
    private static Map<String, FieldInfo> getCriteriaFieldInfo(Class<?> classType) {
        return CRITERIA_CACHE.computeIfAbsent(classType, CriteriaHelper::extractCriteriaFields);
    }

    /**
     * Extracts criteria fields using reflection with enhanced stream processing.
     */
    private static Map<String, FieldInfo> extractCriteriaFields(Class<?> classType) {
        return Stream.of(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Criteria.class))
                .collect(HashMap::new,
                        (map, field) -> map.put(field.getName(),
                                new FieldInfo(field.getName(), field.getType(), field.getType().getSimpleName())),
                        HashMap::putAll);
    }

    /**
     * Builds JPA specification with enhanced validation and error handling.
     *
     * @param <T>       the entity type
     * @param tenant    the tenant identifier
     * @param criteria  the query criteria list
     * @param classType the entity class type
     * @return the JPA specification
     */
    public static <T extends IIdAssignable> Specification<T> buildSpecification(
            String tenant, List<QueryCriteria> criteria, Class<?> classType) {

        var criteriaFields = getCriteriaFieldInfo(classType);
        var specification = Specification.<T>where(null);

        for (var criterion : criteria) {
            validateCriterion(criterion, criteriaFields);
            var criteriaSpec = buildCriteriaSpecification(criterion, criteriaFields);
            specification = combineCriteria(specification, (Specification<T>) criteriaSpec, criterion.getCombiner());
        }

        return addTenantFilter(specification, tenant);
    }

    /**
     * Validates a single criterion against available fields.
     */
    private static void validateCriterion(QueryCriteria criterion, Map<String, FieldInfo> criteriaFields) {
        var fieldName = criterion.getName();
        if (!criteriaFields.containsKey(fieldName)) {
            throw new WrongCriteriaFilterException("Invalid field name: " + fieldName);
        }
    }

    /**
     * Builds specification for a single criterion with type-aware processing.
     */
    private static <T extends IIdAssignable> Specification<T> buildCriteriaSpecification(
            QueryCriteria criterion, Map<String, FieldInfo> criteriaFields) {

        var fieldName = criterion.getName();
        var value = criterion.getValue();
        var fieldInfo = criteriaFields.get(fieldName);

        // Convert value to appropriate type based on field type
        var convertedValue = convertValue(value, fieldInfo.type());

        return switch (criterion.getOperator()) {
            case EQ -> equal(fieldName, convertedValue);
            case NE -> notEqual(fieldName, convertedValue);
            case LI -> like(fieldName, convertedValue.toString());
            case NL -> notLike(fieldName, convertedValue.toString());
            case LT -> lessThan(fieldName, convertedValue);
            case LE -> lessThanOrEqualTo(fieldName, convertedValue);
            case GT -> greaterThan(fieldName, convertedValue);
            case GE -> greaterThanOrEqualTo(fieldName, convertedValue);
            default -> throw new WrongCriteriaFilterException("Unsupported operator: " + criterion.getOperator());
        };
    }

    /**
     * Converts string value to appropriate type based on field type.
     */
    private static Object convertValue(String value, Class<?> fieldType) {
        if (value == null) {
            return null;
        }

        try {
            return switch (fieldType.getSimpleName()) {
                case "String" -> value;
                case "Integer", "int" -> Integer.valueOf(value);
                case "Long", "long" -> Long.valueOf(value);
                case "Double", "double" -> Double.valueOf(value);
                case "Float", "float" -> Float.valueOf(value);
                case "Boolean", "boolean" -> Boolean.valueOf(value);
                default -> value; // Fallback to string
            };
        } catch (NumberFormatException e) {
            log.warn("Failed to convert value '{}' to type {}, using string representation", value, fieldType.getSimpleName());
            return value;
        }
    }

    /**
     * Combines specifications based on the combiner type.
     */
    private static <T extends IIdAssignable> Specification<T> combineCriteria(
            Specification<T> specification, Specification<T> criteriaSpec, IEnumCriteriaCombiner.Types combiner) {

        return switch (combiner) {
            case AND -> specification.and(criteriaSpec);
            case OR -> specification.or(criteriaSpec);
            default -> specification.or(criteriaSpec);
        };
    }

    /**
     * Adds tenant filter if tenant is specified.
     */
    private static <T extends IIdAssignable> Specification<T> addTenantFilter(
            Specification<T> specification, String tenant) {

        if (StringUtils.hasText(tenant)) {
            return specification.and(equal("tenant", tenant));
        }
        return specification;
    }

    /**
     * Like specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> like(String fieldName, String value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get(fieldName), "%" + value + "%");
    }

    // Enhanced specification methods with better type handling

    /**
     * Not like specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> notLike(String fieldName, String value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notLike(root.get(fieldName), "%" + value + "%");
    }

    /**
     * Equal specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> equal(String fieldName, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(fieldName), value);
    }

    /**
     * Not equal specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    public static <T extends IIdAssignable> Specification<T> notEqual(String fieldName, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get(fieldName), value);
    }

    /**
     * Less than specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    @SuppressWarnings("unchecked")
    public static <T extends IIdAssignable> Specification<T> lessThan(String fieldName, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get(fieldName), (Comparable<Object>) value);
    }

    /**
     * Less than or equal to specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    @SuppressWarnings("unchecked")
    public static <T extends IIdAssignable> Specification<T> lessThanOrEqualTo(String fieldName, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), (Comparable<Object>) value);
    }

    /**
     * Greater than specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    @SuppressWarnings("unchecked")
    public static <T extends IIdAssignable> Specification<T> greaterThan(String fieldName, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get(fieldName), (Comparable<Object>) value);
    }

    /**
     * Greater than or equal to specification.
     *
     * @param <T>       the type parameter
     * @param fieldName the field name
     * @param value     the value
     * @return the specification
     */
    @SuppressWarnings("unchecked")
    public static <T extends IIdAssignable> Specification<T> greaterThanOrEqualTo(String fieldName, Object value) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), (Comparable<Object>) value);
    }

    /**
     * Clears the criteria cache. Useful for testing or when class definitions change.
     */
    public static void clearCache() {
        CRITERIA_CACHE.clear();
    }

    /**
     * Gets the size of the criteria cache.
     *
     * @return the cache size
     */
    public static int getCacheSize() {
        return CRITERIA_CACHE.size();
    }

    /**
     * Record to hold field information for better type safety and performance.
     */
    private record FieldInfo(String name, Class<?> type, String typeSimpleName) {
    }
}