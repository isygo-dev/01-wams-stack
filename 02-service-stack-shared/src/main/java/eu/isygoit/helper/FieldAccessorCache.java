package eu.isygoit.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to cache and provide {@link MethodHandle} for field access.
 * This improves performance by avoiding repeated reflection calls and setAccessible(true).
 */
@Slf4j
public class FieldAccessorCache {

    private static final Cache<Class<?>, List<FieldAccessor>> CACHE = Caffeine.newBuilder()
            .build();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Retrieves or computes the list of field accessors for a given class, including its hierarchy.
     *
     * @param clazz the class to get accessors for
     * @return a list of {@link FieldAccessor} for all non-static fields in the class hierarchy
     */
    public static List<FieldAccessor> getAccessors(Class<?> clazz) {
        return CACHE.get(clazz, FieldAccessorCache::computeAccessors);
    }

    private static List<FieldAccessor> computeAccessors(Class<?> clazz) {
        List<FieldAccessor> accessors = new ArrayList<>();
        Class<?> cursor = clazz;
        while (cursor != null && cursor != Object.class) {
            for (Field field : cursor.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    MethodHandle getter = LOOKUP.unreflectGetter(field);
                    accessors.add(new FieldAccessor(field.getName(), getter));
                } catch (IllegalAccessException e) {
                    log.error("Failed to create MethodHandle for field: {} in class: {}", field.getName(), cursor.getName(), e);
                }
            }
            cursor = cursor.getSuperclass();
        }
        return accessors;
    }

    /**
     * Holds the field name and its corresponding {@link MethodHandle} getter.
     */
    public record FieldAccessor(String name, MethodHandle getter) {
        /**
         * Gets the value of the field from the given target object.
         *
         * @param target the object to get the field value from
         * @return the field value
         */
        public Object get(Object target) {
            try {
                return getter.invoke(target);
            } catch (Throwable e) {
                log.error("Failed to invoke MethodHandle getter for field: {}", name, e);
                return null;
            }
        }
    }
}
