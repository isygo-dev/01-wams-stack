package eu.isygoit.helper;

import eu.isygoit.dto.IIdentifiableDto;
import lombok.extern.slf4j.Slf4j;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * The type Bean helper.
 */
@Slf4j
public final class BeanHelper {

    /**
     * Call setter.
     *
     * @param obj       the obj
     * @param fieldName the field name
     * @param value     the value
     */
    public static void callSetter(Object obj, String fieldName, Object value) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
            pd.getWriteMethod().invoke(obj, value);
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            log.error("<Error>: calling setter for {}/{}", obj.getClass().getSimpleName(), fieldName);
        }
    }

    /**
     * Call getter t.
     *
     * @param <T>       the type parameter
     * @param obj       the obj
     * @param fieldName the field name
     * @return the t
     */
    public static <T> T callGetter(Object obj, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
            return (T) pd.getReadMethod().invoke(obj);
        } catch (IntrospectionException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            log.error("<Error>: calling getter/is for {}/{}", obj.getClass().getSimpleName(), fieldName);
        }

        return null;
    }

    /**
     * Merge identifiable dto.
     *
     * @param source      the source
     * @param destination the destination
     * @return the identifiable dto
     */
    public static IIdentifiableDto merge(IIdentifiableDto source, IIdentifiableDto destination) {
        if (source == null || destination == null) {
            log.error("<Error>: merging null objects");
            return destination;
        }
        if (destination.getClass().isAssignableFrom(source.getClass())
                || source.getClass().isAssignableFrom(destination.getClass())) {
            for (Field field : source.getClass().getDeclaredFields()) {
                Object fieldValue = callGetter(source, field.getName());
                if (fieldValue != null) {
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        Collection collection = callGetter(destination, field.getName());
                        if (collection == null) {
                            if (List.class.isAssignableFrom(field.getType())) {
                                collection = new ArrayList();
                            } else if (Set.class.isAssignableFrom(field.getType())) {
                                collection = new HashSet<>();
                            }
                        }

                        if (collection != null) {
                            collection.addAll((Collection) fieldValue);
                            callSetter(destination, field.getName(), collection);
                        }
                    } else {
                        callSetter(destination, field.getName(), fieldValue);
                    }
                }
            }
        } else {
            log.error("<Error>: Error merging object icompatible {}/{}", source.getClass().getSimpleName(), destination.getClass().getSimpleName());
        }
        return destination;
    }
}
