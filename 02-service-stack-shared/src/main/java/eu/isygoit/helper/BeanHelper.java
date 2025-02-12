package eu.isygoit.helper;

import eu.isygoit.dto.IIdentifiableDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * A utility class to handle bean manipulation, including merging and reflection-based field access.
 */
public interface BeanHelper {

    Logger logger = LoggerFactory.getLogger(BeanHelper.class);

    /**
     * Calls the setter method for the given field and value on the specified object.
     *
     * @param obj       the object to set the value on.
     * @param fieldName the field to set the value for.
     * @param value     the value to set.
     */
    static void callSetter(Object obj, String fieldName, Object value) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, obj.getClass());
            pd.getWriteMethod().invoke(obj, value);
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            logger.error("Error calling setter for {}/{}", obj.getClass().getSimpleName(), fieldName, e);
        }
    }

    /**
     * Calls the getter method for the given field on the specified object.
     *
     * @param <E>       the type of the field.
     * @param obj       the object to get the value from.
     * @param fieldName the field to get the value for.
     * @return the field value.
     */
    static <E> E callGetter(Object obj, String fieldName) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(fieldName, obj.getClass());
            return (E) pd.getReadMethod().invoke(obj);
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            logger.error("Error calling getter for {}/{}", obj.getClass().getSimpleName(), fieldName, e);
        }
        return null;
    }

    /**
     * Merges the fields of the source object into the destination object. Non-null values are copied over,
     * and collections are merged (with duplicates added).
     *
     * @param source      the source object to merge.
     * @param destination the destination object.
     * @return the merged destination object.
     */
    static IIdentifiableDto merge(IIdentifiableDto source, IIdentifiableDto destination) {
        if (source == null || destination == null) {
            logger.error("Error: Cannot merge null objects.");
            return destination;
        }

        if (destination.getClass().isAssignableFrom(source.getClass()) || source.getClass().isAssignableFrom(destination.getClass())) {
            for (Field field : source.getClass().getDeclaredFields()) {
                Object fieldValue = callGetter(source, field.getName());
                if (fieldValue != null) {
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        mergeCollectionField(field, fieldValue, destination);
                    } else {
                        callSetter(destination, field.getName(), fieldValue);
                    }
                }
            }
        } else {
            logger.error("Error: Incompatible objects for merging {} and {}", source.getClass().getSimpleName(), destination.getClass().getSimpleName());
        }
        return destination;
    }

    /**
     * Merges a collection field from source to destination by adding elements safely.
     *
     * @param field       the field to merge.
     * @param fieldValue  the collection value from the source.
     * @param destination the destination object.
     */
    private static void mergeCollectionField(Field field, Object fieldValue, Object destination) {
        if (fieldValue instanceof Collection<?>) {
            Collection<?> sourceCollection = (Collection<?>) fieldValue;
            Collection<Object> destinationCollection = Optional.ofNullable(callGetter(destination, field.getName()))
                    .filter(Collection.class::isInstance)
                    .map(Collection.class::cast)
                    .orElseGet(() -> instantiateCollection(field));

            sourceCollection.forEach(destinationCollection::add);
            callSetter(destination, field.getName(), destinationCollection);
        } else {
            logger.warn("Field '{}' in source is not a Collection, skipping merge.", field.getName());
        }
    }

    /**
     * Instantiates a collection (List or Set) based on the field type.
     *
     * @param field the field to instantiate the collection for.
     * @return the instantiated collection.
     */
    private static Collection<Object> instantiateCollection(Field field) {
        if (List.class.isAssignableFrom(field.getType())) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(field.getType())) {
            return new HashSet<>();
        }
        return Collections.emptyList(); // Default to an empty List if unknown type
    }

    /**
     * Copies all fields from the source object to the destination object.
     *
     * @param source      the source object to copy from.
     * @param destination the destination object to copy to.
     * @return the copied destination object.
     */
    static IIdentifiableDto copyFields(IIdentifiableDto source, IIdentifiableDto destination) {
        if (source == null || destination == null) {
            logger.error("Error: Cannot copy fields for null objects.");
            return destination;
        }

        for (Field field : source.getClass().getDeclaredFields()) {
            Object fieldValue = callGetter(source, field.getName());
            if (fieldValue != null) {
                callSetter(destination, field.getName(), fieldValue);
            }
        }
        return destination;
    }

    /**
     * Creates a new instance of a given class using reflection.
     *
     * @param clazz the class to instantiate.
     * @return the new object instance.
     */
    static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Error creating instance of {}", clazz.getSimpleName(), e);
        }
        return null;
    }

    /**
     * Converts a collection to a different type (List to Set or vice versa).
     *
     * @param collection the collection to convert.
     * @param <T>        the type of elements in the collection.
     * @return a new collection of the desired type.
     */
    static <T> Collection<T> convertCollection(Collection<T> collection, Class<? extends Collection> targetType) {
        if (collection == null) return Collections.emptyList();

        if (targetType.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>(collection);
        } else if (targetType.isAssignableFrom(HashSet.class)) {
            return new HashSet<>(collection);
        }
        return collection; // Default to the same type if the target type is unknown
    }
}