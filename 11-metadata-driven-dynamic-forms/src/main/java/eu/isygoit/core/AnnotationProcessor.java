package eu.isygoit.core;

import eu.isygoit.annotation.FormConditional;
import eu.isygoit.annotation.FormField;
import eu.isygoit.annotation.FormList;
import eu.isygoit.domain.*;
import jakarta.validation.constraints.*;

import java.lang.reflect.Field;
import java.util.*;

public class AnnotationProcessor {

    public FieldMetaData processField(Field field, int depth) {
        if (depth > 5) return null;

        FormField formField = field.getAnnotation(FormField.class);
        if (formField == null) return null;

        String key = formField.key().isBlank() ? field.getName() : formField.key();
        String label = formField.label().isBlank() ? capitalize(field.getName()) : formField.label();

        FieldType type = determineFieldType(formField.type(), field.getType());

        Map<String, Object> validation = buildValidationMap(field, formField);
        Map<String, Object> ui = buildUiMap(formField);

        // ==================== CONDITIONAL LOGIC WITH @FormConditional ====================
        FormConditional formConditional = field.getAnnotation(FormConditional.class);

        String visibleWhen = formConditional != null ? formConditional.visibleWhen() : formField.visibleWhen();
        String enabledWhen = formConditional != null ? formConditional.enabledWhen() : formField.enabledWhen();
        String requiredWhen = formConditional != null ? formConditional.requiredWhen() : formField.requiredWhen();

        Map<String, Object> customConditions = new LinkedHashMap<>();

        if (formConditional != null) {
            // Add simple conditions to customConditions for easier frontend access
            if (!visibleWhen.isBlank()) customConditions.put("visibleWhen", visibleWhen);
            if (!enabledWhen.isBlank()) customConditions.put("enabledWhen", enabledWhen);
            if (!requiredWhen.isBlank()) customConditions.put("requiredWhen", requiredWhen);

            // Add all @Condition annotations
            for (eu.isygoit.annotation.Condition cond : formConditional.value()) {
                customConditions.put(cond.key(), cond.value());
            }
        }

        ConditionalRule conditional = new ConditionalRule(
                visibleWhen,
                enabledWhen,
                requiredWhen,
                customConditions
        );

        // Nested Object Support
        List<FieldMetaData> children = (type == FieldType.OBJECT && !isPrimitiveOrWrapper(field.getType()))
                ? scanNestedObject(field.getType(), depth + 1)
                : List.of();

        // List Support
        ListConfig listConfig = null;
        FormList formList = field.getAnnotation(FormList.class);
        if (formList != null && (type == FieldType.LIST || type == FieldType.SET || type == FieldType.TABLE)) {
            listConfig = new ListConfig(
                    "",
                    formList.editable(),
                    formList.sortable(),
                    formList.minItems(),
                    formList.maxItems(),
                    formList.emptyStateMessage(),
                    formList.addButtonLabel(),
                    Arrays.asList(formList.actions()),
                    Arrays.asList(formList.bulkActions()),
                    Map.of()
            );
        }

        return new FieldMetaData(
                key,
                label,
                type,
                formField.required(),
                formField.defaultValue().isBlank() ? null : formField.defaultValue(),
                validation,
                ui,
                children,
                listConfig,
                null,
                conditional
        );
    }

    private List<FieldMetaData> scanNestedObject(Class<?> nestedClass, int depth) {
        List<FieldMetaData> children = new ArrayList<>();
        for (Field field : nestedClass.getDeclaredFields()) {
            if (field.isSynthetic() || java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            FieldMetaData child = processField(field, depth);
            if (child != null) {
                children.add(child);
            }
        }
        return children;
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz == String.class ||
                Number.class.isAssignableFrom(clazz) ||
                Boolean.class == clazz ||
                Character.class == clazz ||
                java.time.temporal.Temporal.class.isAssignableFrom(clazz);
    }

    private FieldType determineFieldType(FieldType declared, Class<?> javaType) {
        if (declared != FieldType.AUTO) return declared;
        if (String.class.equals(javaType)) return FieldType.TEXT;
        if (Integer.class.equals(javaType) || int.class.equals(javaType)) return FieldType.INTEGER;
        if (Double.class.equals(javaType) || double.class.equals(javaType)) return FieldType.DECIMAL;
        if (Boolean.class.equals(javaType) || boolean.class.equals(javaType)) return FieldType.CHECKBOX;
        if (java.time.LocalDate.class.equals(javaType) || java.time.LocalDateTime.class.equals(javaType))
            return FieldType.DATE;
        return FieldType.TEXT;
    }

    private Map<String, Object> buildValidationMap(Field field, FormField ff) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ff.required()) map.put("required", true);
        if (ff.minLength() > 0) map.put("minLength", ff.minLength());
        if (ff.maxLength() > 0) map.put("maxLength", ff.maxLength());
        if (ff.minValue() != Double.MIN_VALUE) map.put("minValue", ff.minValue());
        if (ff.maxValue() != Double.MAX_VALUE) map.put("maxValue", ff.maxValue());
        if (!ff.pattern().isBlank()) map.put("pattern", ff.pattern());

        if (field.isAnnotationPresent(NotNull.class) || field.isAnnotationPresent(NotBlank.class)) {
            map.put("required", true);
        }
        if (field.isAnnotationPresent(Size.class)) {
            Size size = field.getAnnotation(Size.class);
            if (size.min() > 0) map.put("minLength", size.min());
            if (size.max() < Integer.MAX_VALUE) map.put("maxLength", size.max());
        }
        return map;
    }

    private Map<String, Object> buildUiMap(FormField ff) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("display", ff.display());
        map.put("disabled", ff.disabled());
        map.put("readonly", ff.readonly());
        map.put("placeholder", ff.placeholder());
        map.put("helpText", ff.helpText());
        map.put("tooltip", ff.tooltip());
        map.put("rows", ff.rows());

        if (ff.useMask() && !ff.mask().isBlank()) map.put("mask", ff.mask());
        if (!ff.prefix().isBlank()) map.put("prefix", ff.prefix());
        if (!ff.suffix().isBlank()) map.put("suffix", ff.suffix());
        if (!ff.thousandSeparator().isBlank()) map.put("thousandSeparator", ff.thousandSeparator());
        if (!ff.decimalSeparator().isBlank()) map.put("decimalSeparator", ff.decimalSeparator());

        return map;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}