package eu.isygoit.helper;

import eu.isygoit.dto.IDto;
import eu.isygoit.exception.BadFieldNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A utility class to handle bean manipulation, including merging and reflection-based field access.
 * Prefers getters/setters when available, falls back to direct field reflection otherwise.
 */
public interface BeanHelper {

    Logger logger = LoggerFactory.getLogger(BeanHelper.class);

    // ========================================================================
    // Getter with fallback to direct field access
    // ========================================================================
    static <E> E callGetter(Object obj, String fieldName, boolean ignoreIfNotExists) {
        if (obj == null || fieldName == null || fieldName.isBlank()) {
            return null;
        }

        Class<?> clazz = obj.getClass();

        // 1. Try PropertyDescriptor (standard getter / isXXX)
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
            Method readMethod = pd.getReadMethod();
            if (readMethod != null) {
                return (E) readMethod.invoke(obj);
            }
        } catch (Exception ignored) {
            // PropertyDescriptor failed → fallback
        }

        // 2. Fallback: Direct field access (private fields, no getter)
        try {
            Field field = findField(clazz, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return (E) field.get(obj);
            }
        } catch (Exception e) {
            logger.debug("Direct field get failed for {}/{}", clazz.getSimpleName(), fieldName, e);
        }

        // 3. Not found
        if (!ignoreIfNotExists) {
            logger.error("Failed to get field {}/{}", clazz.getSimpleName(), fieldName);
            throw new BadFieldNameException("No readable property or field found: " + fieldName);
        }

        return null;
    }

    // ========================================================================
    // Setter with fallback to direct field access
    // ========================================================================
    static void callSetter(Object obj, String fieldName, Object value, boolean ignoreIfNotExists) {
        if (obj == null || fieldName == null || fieldName.isBlank()) {
            return;
        }

        Class<?> clazz = obj.getClass();

        // 1. Try PropertyDescriptor (standard setter)
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, clazz);
            Method writeMethod = pd.getWriteMethod();
            if (writeMethod != null) {
                writeMethod.invoke(obj, value);
                return;
            }
        } catch (Exception ignored) {
            // fallback
        }

        // 2. Fallback: Direct field access
        try {
            Field field = findField(clazz, fieldName);
            if (field != null) {
                field.setAccessible(true);
                field.set(obj, value);
                return;
            }
        } catch (Exception e) {
            logger.debug("Direct field set failed for {}/{}", clazz.getSimpleName(), fieldName, e);
        }

        // 3. Not found
        if (!ignoreIfNotExists) {
            logger.error("Failed to set field {}/{}", clazz.getSimpleName(), fieldName);
            throw new BadFieldNameException("No writable property or field found: " + fieldName);
        }
    }

    // ========================================================================
    // Core Methods
    // ========================================================================

    static IDto merge(IDto source, IDto destination) {
        if (source == null || destination == null) {
            logger.error("Error: Cannot merge null objects.");
            return destination;
        }

        if (!areCompatible(source, destination)) {
            logger.error("Error: Incompatible objects for merging {} and {}",
                    source.getClass().getSimpleName(), destination.getClass().getSimpleName());
            return destination;
        }

        for (Field field : getAllFields(source.getClass())) {
            String fieldName = field.getName();

            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            try {
                Object fieldValue = callGetter(source, fieldName, true);
                if (fieldValue != null) {
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        mergeCollectionField(field, fieldValue, destination);
                    } else {
                        callSetter(destination, fieldName, fieldValue, true);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to merge field '{}' from {} to {}",
                        fieldName, source.getClass().getSimpleName(), destination.getClass().getSimpleName(), e);
            }
        }
        return destination;
    }

    static IDto copyFields(IDto source, IDto destination) {
        if (source == null || destination == null) {
            logger.error("Error: Cannot copy fields for null objects.");
            return destination;
        }

        for (Field field : getAllFields(source.getClass())) {
            String fieldName = field.getName();

            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }

            try {
                // Prefer getter, fallback to direct field
                Object fieldValue = callGetter(source, fieldName, true);
                if (fieldValue == null) {
                    fieldValue = getFieldValueDirect(source, field);
                }

                if (fieldValue != null) {
                    // Prefer setter, fallback to direct field
                    callSetter(destination, fieldName, fieldValue, true);
                }
            } catch (Exception e) {
                logger.warn("Failed to copy field '{}' from {} to {}",
                        fieldName, source.getClass().getSimpleName(),
                        destination.getClass().getSimpleName(), e);
            }
        }
        return destination;
    }

    // ========================================================================
    // Collection Handling
    // ========================================================================

    static void mergeCollectionField(Field field, Object fieldValue, Object destination) {
        if (!(fieldValue instanceof Collection<?> sourceCollection)) {
            return;
        }

        Collection<Object> destCollection = Optional.ofNullable(callGetter(destination, field.getName(), true))
                .filter(Collection.class::isInstance)
                .map(Collection.class::cast)
                .map(existing -> isImmutableCollection(existing)
                        ? new ArrayList<>(existing)
                        : existing)
                .orElseGet(() -> instantiateCollection(field));

        if (destCollection == null) {
            logger.warn("Could not instantiate collection for field: {}", field.getName());
            return;
        }

        destCollection.addAll(sourceCollection);
        callSetter(destination, field.getName(), destCollection, true);
    }

    static boolean isImmutableCollection(Collection<?> collection) {
        try {
            collection.add(null);
            collection.remove(null);
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        } catch (Exception e) {
            return true; // safer default
        }
    }

    static Collection<Object> instantiateCollection(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(field.getType())) {
            return new HashSet<>();
        }
        return new ArrayList<>(); // default
    }

    // ========================================================================
    // Reflection Helpers
    // ========================================================================

    private static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    private static Object getFieldValueDirect(Object obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            logger.debug("Direct field get failed for {}", field.getName(), e);
            return null;
        }
    }

    private static boolean areCompatible(IDto source, IDto destination) {
        return destination.getClass().isAssignableFrom(source.getClass()) ||
                source.getClass().isAssignableFrom(destination.getClass());
    }

    // ========================================================================
    // Other utilities
    // ========================================================================

    static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Error creating instance of {}", clazz.getSimpleName(), e);
            return null;
        }
    }

    static <T> Collection<T> convertCollection(Collection<T> collection, Class<? extends Collection> targetType) {
        if (collection == null) return Collections.emptyList();

        if (targetType.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>(collection);
        } else if (targetType.isAssignableFrom(HashSet.class)) {
            return new HashSet<>(collection);
        }
        return new ArrayList<>(collection);
    }
}