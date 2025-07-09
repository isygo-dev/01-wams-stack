package eu.isygoit.helper;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.exception.WrongCriteriaFilterException;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Data;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CriteriaHelper Test Suite")
class CriteriaHelperTest {

    @BeforeEach
    void setUp() {
        // Clear cache before each test to ensure isolation
        CriteriaHelper.clearCache();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        CriteriaHelper.clearCache();
    }

    @Nested
    @DisplayName("SQL WHERE Clause Conversion Tests")
    class SqlWhereConversionTests {

        @Test
        @DisplayName("Should convert simple conditions with different operators")
        void testConvertSqlWhereToCriteria_SimpleConditions() {
            String sqlWhere = "name = 'John' & age > 25 | status != 'INACTIVE'";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(3, criteria.size());

            // First condition
            assertEquals("name", criteria.get(0).getName());
            assertEquals(IEnumOperator.Types.EQ, criteria.get(0).getOperator());
            assertEquals("John", criteria.get(0).getValue());
            assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner());

            // Second condition (AND combiner)
            assertEquals("age", criteria.get(1).getName());
            assertEquals(IEnumOperator.Types.GT, criteria.get(1).getOperator());
            assertEquals("25", criteria.get(1).getValue());
            assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(1).getCombiner());

            // Third condition (OR combiner)
            assertEquals("status", criteria.get(2).getName());
            assertEquals(IEnumOperator.Types.NE, criteria.get(2).getOperator());
            assertEquals("INACTIVE", criteria.get(2).getValue());
            assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(2).getCombiner());
        }

        @ParameterizedTest
        @DisplayName("Should handle all supported operators")
        @CsvSource({
                "name = 'John', EQ, John",
                "age != 25, NE, 25",
                "name ~ 'Smith', LI, Smith",
                "email !~ 'test', NL, test",
                "score < 80, LT, 80",
                "score <= 80, LE, 80",
                "age > 18, GT, 18",
                "age >= 21, GE, 21"
        })
        void testConvertSqlWhereToCriteria_AllOperators(String condition, String expectedOperator, String expectedValue) {
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(condition);

            assertEquals(1, criteria.size());
            assertEquals(IEnumOperator.Types.valueOf(expectedOperator), criteria.get(0).getOperator());
            assertEquals(expectedValue, criteria.get(0).getValue());
        }

        @Test
        @DisplayName("Should handle quoted values correctly")
        void testConvertSqlWhereToCriteria_QuotedValues() {
            String sqlWhere = "name = 'John Doe' & description = \"Test Description\" | id = 123";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(3, criteria.size());
            assertEquals("John Doe", criteria.get(0).getValue());
            assertEquals("Test Description", criteria.get(1).getValue());
            assertEquals("123", criteria.get(2).getValue());
        }

        @Test
        @DisplayName("Should handle boolean values")
        void testConvertSqlWhereToCriteria_BooleanValues() {
            String sqlWhere = "active = true & verified != false";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(2, criteria.size());
            assertEquals("true", criteria.get(0).getValue());
            assertEquals("false", criteria.get(1).getValue());
        }

        @Test
        @DisplayName("Should handle WHERE keyword")
        void testConvertSqlWhereToCriteria_WithWhereKeyword() {
            String sqlWhere = "WHERE name = 'Test' & age > 18";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(2, criteria.size());
            assertEquals("name", criteria.get(0).getName());
            assertEquals("age", criteria.get(1).getName());
        }

        @Test
        @DisplayName("Should handle complex parentheses grouping")
        void testConvertSqlWhereToCriteria_ComplexParentheses() {
            String sqlWhere = "(name = 'John' & age > 25) | (department = 'IT' & status = 'ACTIVE')";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(4, criteria.size());

            // First group
            assertEquals("name", criteria.get(0).getName());
            assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner());

            assertEquals("age", criteria.get(1).getName());
            assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(1).getCombiner());

            // Second group
            assertEquals("department", criteria.get(2).getName());
            assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(2).getCombiner());

            assertEquals("status", criteria.get(3).getName());
            assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(3).getCombiner());
        }

        @Test
        @DisplayName("Should handle nested parentheses")
        void testConvertSqlWhereToCriteria_NestedParentheses() {
            String sqlWhere = "department = 'IT' & (name = 'John' | (age > 30 & status = 'SENIOR'))";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(4, criteria.size());
            assertEquals("department", criteria.get(0).getName());
            assertEquals("name", criteria.get(1).getName());
            assertEquals("age", criteria.get(2).getName());
            assertEquals("status", criteria.get(3).getName());
        }

        @Test
        @DisplayName("Should handle single condition in parentheses")
        void testConvertSqlWhereToCriteria_SingleConditionInParentheses() {
            String sqlWhere = "(name = 'John')";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(1, criteria.size());
            assertEquals("name", criteria.get(0).getName());
            assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner());
        }

        @Test
        @DisplayName("Should handle whitespace correctly")
        void testConvertSqlWhereToCriteria_WithWhitespace() {
            String sqlWhere = "  name   =   'John'   &   age   >   25  ";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(2, criteria.size());
            assertEquals("name", criteria.get(0).getName());
            assertEquals("John", criteria.get(0).getValue());
            assertEquals("age", criteria.get(1).getName());
            assertEquals("25", criteria.get(1).getValue());
        }

        @ParameterizedTest
        @DisplayName("Should return empty list for null or empty input")
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        void testConvertSqlWhereToCriteria_EmptyInput(String input) {
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(input);
            assertTrue(criteria.isEmpty());
        }

        @Test
        @DisplayName("Should return empty list for null input")
        void testConvertSqlWhereToCriteria_NullInput() {
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(null);
            assertTrue(criteria.isEmpty());
        }

        @Test
        @DisplayName("Should throw exception for invalid condition format")
        void testConvertSqlWhereToCriteria_InvalidCondition() {
            String sqlWhere = "name =";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> CriteriaHelper.convertSqlWhereToCriteria(sqlWhere)
            );

            assertEquals("Invalid WHERE clause: name =", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for unsupported operator")
        void testConvertSqlWhereToCriteria_UnsupportedOperator() {
            String sqlWhere = "name === 'John'";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> CriteriaHelper.convertSqlWhereToCriteria(sqlWhere)
            );

            assertEquals("Invalid WHERE clause: name === 'John'", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for malformed parentheses")
        void testConvertSqlWhereToCriteria_MalformedParentheses() {
            String sqlWhere = "name = 'John' & (age > 25";

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> CriteriaHelper.convertSqlWhereToCriteria(sqlWhere)
            );

            assertEquals("Invalid WHERE clause: name = 'John' & (age > 25", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle field names with dots")
        void testConvertSqlWhereToCriteria_FieldNamesWithDots() {
            String sqlWhere = "user.name = 'John' & profile.age > 25";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            assertEquals(2, criteria.size());
            assertEquals("user.name", criteria.get(0).getName());
            assertEquals("profile.age", criteria.get(1).getName());
        }
    }

    @Nested
    @DisplayName("Criteria Data Extraction Tests")
    class CriteriaDataTests {

        @Test
        @DisplayName("Should extract criteria fields correctly")
        void testGetCriteriaData() {
            Map<String, String> criteriaData = CriteriaHelper.getCriteriaData(TestEntity.class);

            assertEquals(8, criteriaData.size());
            assertEquals("String", criteriaData.get("name"));
            assertEquals("Integer", criteriaData.get("age"));
            assertEquals("String", criteriaData.get("status"));
            assertEquals("Boolean", criteriaData.get("active"));
            assertEquals("String", criteriaData.get("department"));
            assertEquals("Double", criteriaData.get("salary"));
            assertEquals("Long", criteriaData.get("score"));
            assertNull(criteriaData.get("nonCriteriaField"));
        }

        @Test
        @DisplayName("Should handle empty class")
        void testGetCriteriaData_EmptyClass() {
            Map<String, String> criteriaData = CriteriaHelper.getCriteriaData(EmptyTestEntity.class);
            assertTrue(criteriaData.isEmpty());
        }

        @Test
        @DisplayName("Should cache criteria data")
        void testGetCriteriaData_Caching() {
            // First call
            Map<String, String> firstCall = CriteriaHelper.getCriteriaData(TestEntity.class);
            assertEquals(1, CriteriaHelper.getCacheSize()); // Cache is populated after first call

            // Second call should use cache
            Map<String, String> secondCall = CriteriaHelper.getCriteriaData(TestEntity.class);
            assertEquals(1, CriteriaHelper.getCacheSize()); // Cache is not populated after second call

            assertEquals(firstCall, secondCall);
            assertTrue(CriteriaHelper.getCacheSize() > 0);
        }

        @Test
        @DisplayName("Should clear cache correctly")
        void testClearCache() {
            CriteriaHelper.getCriteriaData(TestEntity.class);
            assertTrue(CriteriaHelper.getCacheSize() > 0);

            CriteriaHelper.clearCache();
            assertEquals(0, CriteriaHelper.getCacheSize());
        }
    }

    @Nested
    @DisplayName("Specification Building Tests")
    class SpecificationBuildingTests {

        @Test
        @DisplayName("Should build specification with OR combiner")
        void testBuildSpecification_WithOrCombiner() {
            QueryCriteria criteria1 = QueryCriteria.builder()
                    .name("name")
                    .operator(IEnumOperator.Types.EQ)
                    .value("John")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            QueryCriteria criteria2 = QueryCriteria.builder()
                    .name("age")
                    .operator(IEnumOperator.Types.GT)
                    .value("25")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    null, List.of(criteria1, criteria2), TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should build specification with AND combiner")
        void testBuildSpecification_WithAndCombiner() {
            QueryCriteria criteria1 = QueryCriteria.builder()
                    .name("name")
                    .operator(IEnumOperator.Types.EQ)
                    .value("John")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            QueryCriteria criteria2 = QueryCriteria.builder()
                    .name("age")
                    .operator(IEnumOperator.Types.GT)
                    .value("25")
                    .combiner(IEnumCriteriaCombiner.Types.AND)
                    .build();

            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    null, List.of(criteria1, criteria2), TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should build specification with tenant")
        void testBuildSpecification_WithTenant() {
            QueryCriteria criteria = QueryCriteria.builder()
                    .name("name")
                    .operator(IEnumOperator.Types.EQ)
                    .value("John")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    "tenant1", List.of(criteria), TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should build specification without tenant")
        void testBuildSpecification_WithoutTenant() {
            QueryCriteria criteria = QueryCriteria.builder()
                    .name("name")
                    .operator(IEnumOperator.Types.EQ)
                    .value("John")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    null, List.of(criteria), TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should build specification with empty tenant")
        void testBuildSpecification_WithEmptyTenant() {
            QueryCriteria criteria = QueryCriteria.builder()
                    .name("name")
                    .operator(IEnumOperator.Types.EQ)
                    .value("John")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    "", List.of(criteria), TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should throw exception for invalid field name")
        void testBuildSpecification_InvalidFieldName() {
            QueryCriteria criteria = QueryCriteria.builder()
                    .name("invalidField")
                    .operator(IEnumOperator.Types.EQ)
                    .value("value")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            WrongCriteriaFilterException exception = assertThrows(
                    WrongCriteriaFilterException.class,
                    () -> CriteriaHelper.buildSpecification(null, List.of(criteria), TestEntity.class)
            );

            assertTrue(exception.getMessage().contains("Invalid field name: invalidField"));
        }

        @Test
        @DisplayName("Should handle all operator types")
        void testBuildSpecification_AllOperators() {
            IEnumOperator.Types[] operators = {
                    IEnumOperator.Types.EQ, IEnumOperator.Types.NE, IEnumOperator.Types.LI,
                    IEnumOperator.Types.NL, IEnumOperator.Types.LT, IEnumOperator.Types.LE,
                    IEnumOperator.Types.GT, IEnumOperator.Types.GE
            };

            for (IEnumOperator.Types operator : operators) {
                QueryCriteria criteria = QueryCriteria.builder()
                        .name("name")
                        .operator(operator)
                        .value("value")
                        .combiner(IEnumCriteriaCombiner.Types.OR)
                        .build();

                Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                        null, List.of(criteria), TestEntity.class);
                assertNotNull(spec, "Failed for operator: " + operator);
            }
        }

        @Test
        @DisplayName("Should handle type conversion for different field types")
        void testBuildSpecification_TypeConversion() {
            // Test Integer conversion
            QueryCriteria intCriteria = QueryCriteria.builder()
                    .name("age")
                    .operator(IEnumOperator.Types.EQ)
                    .value("25")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            // Test Boolean conversion
            QueryCriteria boolCriteria = QueryCriteria.builder()
                    .name("active")
                    .operator(IEnumOperator.Types.EQ)
                    .value("true")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            // Test Double conversion
            QueryCriteria doubleCriteria = QueryCriteria.builder()
                    .name("salary")
                    .operator(IEnumOperator.Types.GT)
                    .value("50000.0")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            List<QueryCriteria> criteria = List.of(intCriteria, boolCriteria, doubleCriteria);
            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    null, criteria, TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should handle invalid type conversion gracefully")
        void testBuildSpecification_InvalidTypeConversion() {
            QueryCriteria criteria = QueryCriteria.builder()
                    .name("age")
                    .operator(IEnumOperator.Types.EQ)
                    .value("invalid_number")
                    .combiner(IEnumCriteriaCombiner.Types.OR)
                    .build();

            // Should not throw exception, should fallback to string
            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    null, List.of(criteria), TestEntity.class);

            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("Individual Specification Methods Tests")
    class IndividualSpecificationTests {

        @Test
        @DisplayName("Should create equal specification")
        void testEqualSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.equal("name", "John");
            assertNotNull(spec);

            // Test with mock criteria builder
            Root<TestEntity> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);

            spec.toPredicate(root, query, cb);
            verify(cb).equal(any(), eq("John"));
        }

        @Test
        @DisplayName("Should create not equal specification")
        void testNotEqualSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.notEqual("name", "John");
            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should create like specification")
        void testLikeSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.like("name", "John");
            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should create not like specification")
        void testNotLikeSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.notLike("name", "John");
            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should create less than specification")
        void testLessThanSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.lessThan("age", 30);
            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should create less than or equal specification")
        void testLessThanOrEqualToSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.lessThanOrEqualTo("age", 30);
            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should create greater than specification")
        void testGreaterThanSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.greaterThan("age", 30);
            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should create greater than or equal specification")
        void testGreaterThanOrEqualToSpecification() {
            Specification<TestEntity> spec = CriteriaHelper.greaterThanOrEqualTo("age", 30);
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("Utility Class Tests")
    class UtilityClassTests {

        @Test
        @DisplayName("Should not allow instantiation")
        void testPrivateConstructor() {
            assertThrows(UnsupportedOperationException.class, () -> {
                try {
                    var constructor = CriteriaHelper.class.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    constructor.newInstance();
                } catch (Exception e) {
                    if (e.getCause() instanceof UnsupportedOperationException) {
                        throw (UnsupportedOperationException) e.getCause();
                    }
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complex real-world scenario")
        void testComplexRealWorldScenario() {
            String complexQuery = "WHERE (name ~ 'John' | name ~ 'Jane') & age >= 18 & age <= 65 & " +
                    "active = true & (department = 'IT' | department = 'HR') & salary > 50000";

            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(complexQuery);

            assertFalse(criteria.isEmpty());

            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    "tenant1", criteria, TestEntity.class);

            assertNotNull(spec);
        }

        @Test
        @DisplayName("Should handle end-to-end workflow")
        void testEndToEndWorkflow() {
            // 1. Parse SQL WHERE clause
            String sqlWhere = "name = 'John' & age > 25 & active = true";
            List<QueryCriteria> criteria = CriteriaHelper.convertSqlWhereToCriteria(sqlWhere);

            // 2. Verify criteria parsing
            assertEquals(3, criteria.size());

            // 3. Get criteria metadata
            Map<String, String> criteriaData = CriteriaHelper.getCriteriaData(TestEntity.class);
            assertTrue(criteriaData.containsKey("name"));
            assertTrue(criteriaData.containsKey("age"));
            assertTrue(criteriaData.containsKey("active"));

            // 4. Build specification
            Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                    "tenant1", criteria, TestEntity.class);

            assertNotNull(spec);
        }
    }

    // Test entities
    @Data
    static class TestEntity implements IIdAssignable<Long> {
        private Long id;

        @Criteria
        private String name;

        @Criteria
        private Integer age;

        @Criteria
        private String status;

        @Criteria
        private Boolean active;

        @Criteria
        private String department;

        @Criteria
        private Double salary;

        @Criteria
        private Long score;

        private String nonCriteriaField;

        @Criteria
        private String tenant;
    }

    @Data
    static class EmptyTestEntity implements IIdAssignable<Long> {
        private Long id;
        private String someField;
    }
}