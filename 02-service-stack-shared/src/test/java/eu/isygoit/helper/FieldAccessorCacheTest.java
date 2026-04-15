package eu.isygoit.helper;

import eu.isygoit.model.IDirtyEntity;
import eu.isygoit.model.IIdAssignable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FieldAccessorCacheTest {

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseEntity implements IIdAssignable<Long>, IDirtyEntity {
        private Long id;
        private String code;

        @Override
        public Set<String> ignoreFields() {
            return Set.of("id");
        }
    }

    @Data
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleEntity extends BaseEntity {
        private String name;
        private Integer age;
    }

    @Test
    public void testGetAccessors() {
        List<FieldAccessorCache.FieldAccessor> accessors = FieldAccessorCache.getAccessors(SimpleEntity.class);
        assertNotNull(accessors);
        
        // Should contain fields from SimpleEntity and BaseEntity
        assertTrue(accessors.stream().anyMatch(a -> a.name().equals("name")));
        assertTrue(accessors.stream().anyMatch(a -> a.name().equals("age")));
        assertTrue(accessors.stream().anyMatch(a -> a.name().equals("id")));
        assertTrue(accessors.stream().anyMatch(a -> a.name().equals("code")));
    }

    @Test
    public void testFieldAccessorGet() {
        SimpleEntity entity = SimpleEntity.builder()
                .id(1L)
                .code("CODE1")
                .name("John")
                .age(30)
                .build();

        List<FieldAccessorCache.FieldAccessor> accessors = FieldAccessorCache.getAccessors(SimpleEntity.class);
        
        FieldAccessorCache.FieldAccessor nameAccessor = accessors.stream()
                .filter(a -> a.name().equals("name"))
                .findFirst().orElseThrow();
        assertEquals("John", nameAccessor.get(entity));

        FieldAccessorCache.FieldAccessor idAccessor = accessors.stream()
                .filter(a -> a.name().equals("id"))
                .findFirst().orElseThrow();
        assertEquals(1L, idAccessor.get(entity));
    }

    @Test
    public void testDirtyCheckingLogic() {
        SimpleEntity original = SimpleEntity.builder()
                .id(1L)
                .code("CODE1")
                .name("John")
                .age(30)
                .build();

        SimpleEntity incoming = SimpleEntity.builder()
                .id(1L)
                .code("CODE1")
                .name("John Doe") // Changed
                .age(30)
                .build();

        assertTrue(hasDirtyField(incoming, original, incoming.ignoreFields()));

        // Test no changes
        SimpleEntity noChanges = SimpleEntity.builder()
                .id(1L)
                .code("CODE1")
                .name("John")
                .age(30)
                .build();
        assertFalse(hasDirtyField(noChanges, original, noChanges.ignoreFields()));

        // Test ignored field change (id is ignored in BaseEntity)
        SimpleEntity ignoredChange = SimpleEntity.builder()
                .id(2L) // Changed but ignored
                .code("CODE1")
                .name("John")
                .age(30)
                .build();
        assertFalse(hasDirtyField(ignoredChange, original, ignoredChange.ignoreFields()));

        // Test change in superclass field (code)
        SimpleEntity superclassChange = SimpleEntity.builder()
                .id(1L)
                .code("CODE2") // Changed
                .name("John")
                .age(30)
                .build();
        assertTrue(hasDirtyField(superclassChange, original, superclassChange.ignoreFields()));

        // Test with null values
        SimpleEntity nullIncoming = SimpleEntity.builder()
                .id(1L)
                .code(null) // Changed from CODE1
                .name("John")
                .age(30)
                .build();
        assertTrue(hasDirtyField(nullIncoming, original, nullIncoming.ignoreFields()));

        SimpleEntity originalWithNull = SimpleEntity.builder()
                .id(1L)
                .code(null)
                .name("John")
                .age(30)
                .build();
        SimpleEntity incomingWithValue = SimpleEntity.builder()
                .id(1L)
                .code("CODE1") // Changed from null
                .name("John")
                .age(30)
                .build();
        assertTrue(hasDirtyField(incomingWithValue, originalWithNull, incomingWithValue.ignoreFields()));
    }

    // This method replicates the logic in CrudService and CrudTenantService
    private boolean hasDirtyField(Object incoming, Object original, Set<String> ignoredFields) {
        for (FieldAccessorCache.FieldAccessor accessor : FieldAccessorCache.getAccessors(incoming.getClass())) {
            if (ignoredFields != null && ignoredFields.contains(accessor.name())) {
                continue;
            }

            Object incomingValue = accessor.get(incoming);
            Object originalValue = accessor.get(original);

            if (!objectsEqual(incomingValue, originalValue)) {
                return true;
            }
        }
        return false;
    }

    private boolean objectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}
