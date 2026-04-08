package eu.isygoit.core;

import eu.isygoit.annotation.FormView;
import eu.isygoit.domain.FieldMetaData;
import eu.isygoit.domain.ViewMetaData;
import eu.isygoit.exception.MetaDataGenerationException;

import java.util.*;

/**
 * Responsible for scanning classes annotated with @FormView
 * and delegating field processing to AnnotationProcessor.
 * <p>
 * Reflection is performed only once per view (cached externally later).
 */
public class MetaDataScanner {

    private final AnnotationProcessor annotationProcessor;

    public MetaDataScanner(AnnotationProcessor annotationProcessor) {
        this.annotationProcessor = Objects.requireNonNull(annotationProcessor, "AnnotationProcessor cannot be null");
    }

    /**
     * Scans a class and generates ViewMetaData if annotated with @FormView.
     *
     * @param viewClass the class to scan (must be annotated with @FormView)
     * @return ViewMetaData containing all processed fields
     * @throws MetaDataGenerationException if the class is not properly annotated
     */
    public ViewMetaData scan(Class<?> viewClass) {
        FormView formView = viewClass.getAnnotation(FormView.class);
        if (formView == null) {
            throw new MetaDataGenerationException("Class " + viewClass.getName() + " is not annotated with @FormView");
        }

        String name = formView.name();
        String title = formView.title().isBlank() ? name : formView.title();
        String description = formView.description();
        String version = formView.version();

        // Collect all fields including inherited ones
        List<FieldMetaData> fields = scanFields(viewClass);

        // Basic UI config (extensible later)
        Map<String, Object> uiConfig = Map.of(
                "theme", "default",
                "size", "medium"
        );

        Map<String, Object> actions = Map.of(
                "submit", Map.of("label", "Save", "variant", "primary"),
                "cancel", Map.of("label", "Cancel")
        );

        return new ViewMetaData(name, title, description, version, fields, uiConfig, actions);
    }

    /**
     * Recursively scans fields from the class and all superclasses.
     */
    private List<FieldMetaData> scanFields(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return List.of();
        }

        List<FieldMetaData> fields = new ArrayList<>();

        // Process declared fields
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.isSynthetic() || java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            FieldMetaData fieldMetaData = annotationProcessor.processField(field, 0);
            if (fieldMetaData != null) {
                fields.add(fieldMetaData);
            }
        }

        // Recurse to superclass
        fields.addAll(scanFields(clazz.getSuperclass()));

        // Sort by order defined in @FormField
        fields.sort(Comparator.comparingInt(f -> {
            // In real implementation, order would come from annotation - placeholder for now
            return 0;
        }));

        return fields;
    }
}