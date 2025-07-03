package eu.isygoit.helper;

import eu.isygoit.dto.IIdAssignableDto;
import eu.isygoit.exception.BadFieldNameException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Bean helper test.
 */
public class BeanHelperTest {

    private SampleBean source;
    private SampleBean destination;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        source = new SampleBean(1L, "sample bean", "John", "Doe", 30, new Date(), Arrays.asList("A", "B", "C"), new HashSet<>(Arrays.asList("X", "Y")));
        destination = new SampleBean(2L, "sample bean", "Jane", "Smith", 25, new Date(), Arrays.asList("D", "E"), new HashSet<>(Arrays.asList("Z")));
    }

    /**
     * The type Sample bean.
     */
// SampleBean class definition with multiple field types for testing
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder
    public static class SampleBean implements IIdAssignableDto<Long> {
        private Long id;
        private String sectionName;
        private String firstName;
        private String lastName;
        private int age;
        private Date birthDate;
        private List<String> collectionField;
        private Set<String> setField;

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    /**
     * The type Call setter tests.
     */
    @Nested
    @DisplayName("Test callSetter method")
    class CallSetterTests {

        /**
         * Test call setter.
         */
        @Test
        @DisplayName("should set the value of the specified field")
        void testCallSetter() {
            BeanHelper.callSetter(destination, "firstName", "Alice", false);
            assertEquals("Alice", destination.getFirstName());
        }

        /**
         * Should throw exception when field not found.
         */
        @Test
        @DisplayName("Should throw BadFieldNameException if the field is not found")
        void shouldThrowExceptionWhenFieldNotFound() {
            // Given
            String invalidFieldName = "nonExistentField";

            // When & Then
            assertThrows(BadFieldNameException.class, () ->
                            BeanHelper.callSetter(destination, invalidFieldName, "Value", false),
                    "Expected callSetter to throw BadFieldNameException for an invalid field name."
            );
        }

    }

    /**
     * The type Call getter tests.
     */
    @Nested
    @DisplayName("Test callGetter method")
    class CallGetterTests {

        /**
         * Test call getter.
         */
        @Test
        @DisplayName("should get the value of the specified field")
        void testCallGetter() {
            assertEquals("John", BeanHelper.callGetter(source, "firstName", false));
        }

        /**
         * Should throw exception when field not found in getter.
         */
        @Test
        @DisplayName("Should throw BadFieldNameException if the field is not found")
        void shouldThrowExceptionWhenFieldNotFoundInGetter() {
            // Given
            String invalidFieldName = "nonExistentField";

            // When & Then
            assertThrows(BadFieldNameException.class, () ->
                            BeanHelper.callGetter(source, invalidFieldName, false),
                    "Expected callGetter to throw BadFieldNameException for an invalid field name."
            );
        }

        /**
         * Should return null when field not found in getter.
         */
        @Test
        @DisplayName("Should return null if the field is not found")
        void shouldReturnNullWhenFieldNotFoundInGetter() {
            // Given
            String invalidFieldName = "nonExistentField";

            // When & Then
            assertNull(BeanHelper.callGetter(source, invalidFieldName, true),
                    "Expected callGetter to throw BadFieldNameException for an invalid field name."
            );
        }
    }

    /**
     * The type Merge tests.
     */
    @Nested
    @DisplayName("Test merge method")
    class MergeTests {

        /**
         * Test merge non null fields.
         */
        @Test
        @DisplayName("should merge non-null fields from source to destination")
        void testMergeNonNullFields() {
            SampleBean result = (SampleBean) BeanHelper.merge(source, destination);
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals(30, result.getAge());
            assertTrue(result.getCollectionField().contains("A"));
            assertTrue(result.getSetField().contains("X"));
        }

        /**
         * Test merge with null source field.
         */
        @Test
        @DisplayName("should not overwrite destination fields if source has null value")
        void testMergeWithNullSourceField() {
            source.setFirstName(null);
            SampleBean result = (SampleBean) BeanHelper.merge(source, destination);
            assertEquals("Jane", result.getFirstName()); // Should not overwrite with null
        }

        /**
         * Test merge collections.
         */
        @Test
        @DisplayName("should merge collections correctly (with duplicates added)")
        void testMergeCollections() {
            source.setCollectionField(Arrays.asList("A", "B", "C", "D", "E"));
            SampleBean result = (SampleBean) BeanHelper.merge(source, destination);
            assertTrue(result.getCollectionField().contains("A"));
            assertTrue(result.getCollectionField().contains("B"));
            assertTrue(result.getCollectionField().contains("C"));
            assertTrue(result.getCollectionField().contains("D"));
            assertTrue(result.getCollectionField().contains("E"));
        }
    }

    /**
     * The type Copy fields tests.
     */
    @Nested
    @DisplayName("Test copyFields method")
    class CopyFieldsTests {

        /**
         * Test copy fields.
         */
        @Test
        @DisplayName("should copy all non-null fields from source to destination")
        void testCopyFields() {
            SampleBean result = (SampleBean) BeanHelper.copyFields(source, destination);
            assertEquals("John", result.getFirstName());
            assertEquals("Doe", result.getLastName());
            assertEquals(30, result.getAge());
            assertTrue(result.getCollectionField().contains("A"));
        }

        /**
         * Test copy null fields.
         */
        @Test
        @DisplayName("should not copy null fields from source to destination")
        void testCopyNullFields() {
            source.setFirstName(null);
            SampleBean result = (SampleBean) BeanHelper.copyFields(source, destination);
            assertEquals("Jane", result.getFirstName()); // Should not overwrite with null
        }
    }

    /**
     * The type Create instance tests.
     */
    @Nested
    @DisplayName("Test createInstance method")
    class CreateInstanceTests {

        /**
         * Test create instance.
         */
        @Test
        @DisplayName("should create a new instance of SampleBean")
        void testCreateInstance() {
            SampleBean instance = BeanHelper.createInstance(SampleBean.class);
            assertNotNull(instance);
            assertNull(instance.getFirstName()); // Default value of String field
            assertEquals(0, instance.getAge()); // Default value of int field
        }
    }

    /**
     * The type Convert collection tests.
     */
    @Nested
    @DisplayName("Test convertCollection method")
    class ConvertCollectionTests {

        /**
         * Test convert list to set.
         */
        @Test
        @DisplayName("should convert List to Set correctly")
        void testConvertListToSet() {
            List<String> list = Arrays.asList("A", "B", "C");
            Collection<String> set = BeanHelper.convertCollection(list, HashSet.class);
            assertTrue(set instanceof Set);
            assertEquals(3, set.size());
            assertTrue(set.contains("A"));
        }

        /**
         * Test convert set to list.
         */
        @Test
        @DisplayName("should convert Set to List correctly")
        void testConvertSetToList() {
            Set<String> set = new HashSet<>(Arrays.asList("X", "Y", "Z"));
            Collection<String> list = BeanHelper.convertCollection(set, ArrayList.class);
            assertTrue(list instanceof List);
            assertEquals(3, list.size());
            assertTrue(list.contains("X"));
        }
    }
}