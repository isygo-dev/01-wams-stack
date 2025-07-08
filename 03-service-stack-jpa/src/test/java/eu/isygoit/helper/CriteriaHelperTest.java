package eu.isygoit.helper;

import eu.isygoit.annotation.Criteria;
import eu.isygoit.enums.IEnumCriteriaCombiner;
import eu.isygoit.enums.IEnumOperator;
import eu.isygoit.jwt.filter.QueryCriteria;
import eu.isygoit.model.IIdAssignable;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CriteriaHelperTest {

    @Test
    void testConvertSqlWhereToCriteria_SimpleConditions() {
        String sqlWhere = "name = 'John' & age > 25 | status != 'INACTIVE'";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

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

    @Test
    void testConvertSqlWhereToCriteria_WithDifferentOperators() {
        String sqlWhere = "score >= 80 & name ~ 'Smith' | age <= 30";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(3, criteria.size());

        assertEquals(IEnumOperator.Types.GE, criteria.get(0).getOperator());
        assertEquals(IEnumOperator.Types.LI, criteria.get(1).getOperator());
        assertEquals(IEnumOperator.Types.LE, criteria.get(2).getOperator());
    }

    @Test
    void testConvertSqlWhereToCriteria_WithBooleanValues() {
        String sqlWhere = "active = true & verified != false";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(2, criteria.size());
        assertEquals("true", criteria.get(0).getValue());
        assertEquals("false", criteria.get(1).getValue());
    }

    @Test
    void testConvertSqlWhereToCriteria_WithWhereKeyword() {
        String sqlWhere = "WHERE name = 'Test'";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(1, criteria.size());
        assertEquals("name", criteria.get(0).getName());
    }

    @Test
    void testConvertSqlWhereToCriteria_EmptyInput() {
        String sqlWhere = "";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertTrue(criteria.isEmpty());
    }

    @Test
    void testConvertSqlWhereToCriteria_InvalidCondition() {
        String sqlWhere = "name =";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertTrue(criteria.isEmpty());
    }

    // Test class for annotation testing
    @Data
    static class TestEntity implements IIdAssignable<Long>{

        private Long id;
        @Criteria
        private String name;
        @Criteria
        private int age;
        @Criteria
        private String status;

        private String nonCriteriaField;
    }

    @Test
    void testGetCriteriaData() {
        Map<String, String> criteriaData = CriteriaHelper.getCriteriaData(TestEntity.class);

        assertEquals(3, criteriaData.size());
        assertEquals("String", criteriaData.get("name"));
        assertEquals("int", criteriaData.get("age"));
        assertEquals("String", criteriaData.get("status"));
        assertNull(criteriaData.get("nonCriteriaField"));
    }

    @Test
    void testBuildSpecification_WithCombiner() {
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

        Specification<IIdAssignable> spec = CriteriaHelper.buildSpecification(
                "tenant1", List.of(criteria1, criteria2), TestEntity.class);

        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithTenant() {
        QueryCriteria criteria = QueryCriteria.builder()
                .name("name")
                .operator(IEnumOperator.Types.EQ)
                .value("John")
                .build();

        Specification<IIdAssignable> spec = CriteriaHelper.buildSpecification(
                "tenant1", List.of(criteria), TestEntity.class);

        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_WithoutTenant() {
        QueryCriteria criteria = QueryCriteria.builder()
                .name("name")
                .operator(IEnumOperator.Types.EQ)
                .value("John")
                .build();

        Specification<IIdAssignable> spec = CriteriaHelper.buildSpecification(
                null, List.of(criteria), TestEntity.class);

        assertNotNull(spec);
    }

    @Test
    void testBuildSpecification_InvalidCriteria() {
        QueryCriteria criteria = QueryCriteria.builder()
                .name("invalidField")
                .operator(IEnumOperator.Types.EQ)
                .value("value")
                .build();

        assertThrows(RuntimeException.class, () ->
                CriteriaHelper.buildSpecification(null, List.of(criteria), TestEntity.class));
    }

    @Test
    void testEqualSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.equal("name", "John");
        assertNotNull(spec);
    }

    @Test
    void testNotEqualSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.notEqual("name", "John");
        assertNotNull(spec);
    }

    @Test
    void testLikeSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.like("name", "John");
        assertNotNull(spec);
    }

    @Test
    void testNotLikeSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.notLike("name", "John");
        assertNotNull(spec);
    }

    @Test
    void testLessThanSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.lessThan("age", "30");
        assertNotNull(spec);
    }

    @Test
    void testLessThanOrEqualToSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.lessThanOrEqualTo("age", "30");
        assertNotNull(spec);
    }

    @Test
    void testGreaterThanSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.greaterThan("age", "30");
        assertNotNull(spec);
    }

    @Test
    void testGreaterThanOrEqualToSpecification() {
        Specification<IIdAssignable> spec = CriteriaHelper.greaterThanOrEqualTo("age", "30");
        assertNotNull(spec);
    }

    @Test
    void testAllOperatorTypesInBuildSpecification() {
        for (IEnumOperator.Types operator : IEnumOperator.Types.values()) {
            QueryCriteria criteria = QueryCriteria.builder()
                    .name("name")
                    .operator(operator)
                    .value("value")
                    .build();

            if (operator == IEnumOperator.Types.BW) {
                // BETWEEN operator needs special handling
                assertThrows(RuntimeException.class, () ->
                        CriteriaHelper.buildSpecification(null, List.of(criteria), TestEntity.class));
            } else {
                Specification<IIdAssignable> spec = CriteriaHelper.buildSpecification(
                        null, List.of(criteria), TestEntity.class);
                assertNotNull(spec);
            }
        }
    }

    @Test
    void testConvertSqlWhereToCriteria_WithParentheses() {
        String sqlWhere = "(name = 'John' & age > 25) | status = 'ACTIVE'";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(3, criteria.size());

        // First condition (inside parentheses)
        assertEquals("name", criteria.get(0).getName());
        assertEquals(IEnumOperator.Types.EQ, criteria.get(0).getOperator());
        assertEquals("John", criteria.get(0).getValue());
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner()); // Inherited from initial OR

        // Second condition (AND combiner inside parentheses)
        assertEquals("age", criteria.get(1).getName());
        assertEquals(IEnumOperator.Types.GT, criteria.get(1).getOperator());
        assertEquals("25", criteria.get(1).getValue());
        assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(1).getCombiner());

        // Third condition (after parentheses)
        assertEquals("status", criteria.get(2).getName());
        assertEquals(IEnumOperator.Types.EQ, criteria.get(2).getOperator());
        assertEquals("ACTIVE", criteria.get(2).getValue());
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(2).getCombiner());
    }

    @Test
    void testConvertSqlWhereToCriteria_NestedParentheses() {
        String sqlWhere = "department = 'IT' & (name = 'John' | (age > 30 & status = 'SENIOR'))";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(4, criteria.size());

        // First condition
        assertEquals("department", criteria.get(0).getName());
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner());

        // Second condition (inside first parentheses)
        assertEquals("name", criteria.get(1).getName());
        assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(1).getCombiner());

        // Third condition (inside nested parentheses)
        assertEquals("age", criteria.get(2).getName());
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(2).getCombiner());

        // Fourth condition (inside nested parentheses)
        assertEquals("status", criteria.get(3).getName());
        assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(3).getCombiner());
    }

    @Test
    void testConvertSqlWhereToCriteria_ComplexWithParentheses() {
        String sqlWhere = "(name = 'John' | age > 25) & (department = 'IT' | status = 'ACTIVE')";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(4, criteria.size());

        // Verify all combiners are correct
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner()); // name
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(1).getCombiner()); // age
        assertEquals(IEnumCriteriaCombiner.Types.AND, criteria.get(2).getCombiner()); // department
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(3).getCombiner()); // status
    }

    @Test
    void testConvertSqlWhereToCriteria_SingleConditionInParentheses() {
        String sqlWhere = "(name = 'John')";
        List<QueryCriteria> criteria = CriteriaHelper.convertsqlWhereToCriteria(sqlWhere);

        assertEquals(1, criteria.size());
        assertEquals("name", criteria.get(0).getName());
        assertEquals(IEnumCriteriaCombiner.Types.OR, criteria.get(0).getCombiner());
    }

    @Test
    void testBuildSpecification_WithParentheses() {
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

        QueryCriteria criteria3 = QueryCriteria.builder()
                .name("status")
                .operator(IEnumOperator.Types.EQ)
                .value("ACTIVE")
                .combiner(IEnumCriteriaCombiner.Types.OR)
                .build();

        // Simulates: (name = 'John' AND age > 25) OR status = 'ACTIVE'
        Specification<TestEntity> spec = CriteriaHelper.buildSpecification(
                null, List.of(criteria1, criteria2, criteria3), TestEntity.class);

        assertNotNull(spec);
    }
}