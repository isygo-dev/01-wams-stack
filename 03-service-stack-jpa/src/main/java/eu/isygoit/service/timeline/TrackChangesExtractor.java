package eu.isygoit.service.timeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.isygoit.annotation.TrackChanges;

import java.lang.reflect.Field;

public final class TrackChangesExtractor {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private TrackChangesExtractor() {}

    /**
     * Walks the class hierarchy and collects only fields annotated with @TrackChanges.
     * Returns an empty ObjectNode if no annotated fields are found.
     */
    public static JsonNode extract(Object entity) {
        ObjectNode node = MAPPER.createObjectNode();
        Class<?> clazz = entity.getClass();

        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(TrackChanges.class)) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(entity);
                        node.set(field.getName(), MAPPER.valueToTree(value));
                    } catch (IllegalAccessException e) {
                        node.put(field.getName(), "<unreadable>");
                    }
                }
            }
            clazz = clazz.getSuperclass();   // walk up — catches inherited @TrackChanges fields
        }

        return node;
    }
}
