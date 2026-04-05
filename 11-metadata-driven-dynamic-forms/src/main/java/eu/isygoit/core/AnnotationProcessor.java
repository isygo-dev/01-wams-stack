package eu.isygoit.core;

import eu.isygoit.annotation.FormConditional;
import eu.isygoit.annotation.FormField;
import eu.isygoit.annotation.FormList;
import eu.isygoit.annotation.FormOption;
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

        // Rich Options (static @FormOption support)
        OptionsConfig optionsConfig = buildOptionsConfig(formField, field);

        // File Upload
        FileUploadConfig fileUploadConfig = buildFileUploadConfig(formField, type);

        // Conditional
        FormConditional fc = field.getAnnotation(FormConditional.class);
        Map<String, Object> customConditions = buildCustomConditions(fc, formField);

        ConditionalRule conditional = new ConditionalRule(
                fc != null ? fc.visibleWhen() : formField.visibleWhen(),
                fc != null ? fc.enabledWhen() : formField.enabledWhen(),
                fc != null ? fc.requiredWhen() : formField.requiredWhen(),
                customConditions
        );

        // Nested Object
        List<FieldMetaData> children = (type == FieldType.OBJECT && !isPrimitiveOrWrapper(field.getType()))
                ? scanNestedObject(field.getType(), depth + 1)
                : List.of();

        // List Config
        ListConfig listConfig = buildListConfig(field, type);

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
                optionsConfig,
                fileUploadConfig,
                conditional
        );
    }

    private OptionsConfig buildOptionsConfig(FormField ff, Field field) {
        if (ff.type() != FieldType.SELECT && ff.type() != FieldType.MULTISELECT &&
                ff.type() != FieldType.AUTOCOMPLETE && ff.type() != FieldType.RADIO) {
            return null;
        }

        List<OptionItem> staticOptions = new ArrayList<>();

        // Support repeatable @FormOption
        FormOption[] options = field.getAnnotationsByType(FormOption.class);
        for (FormOption opt : options) {
            staticOptions.add(new OptionItem(
                    opt.value(),
                    opt.label(),
                    opt.description(),
                    opt.disabled(),
                    opt.group(),
                    opt.icon(),
                    opt.color(),
                    opt.order(),
                    Map.of()
            ));
        }

        // Sort by order
        staticOptions.sort(Comparator.comparingInt(OptionItem::order));

        return new OptionsConfig(
                ff.multiple(),
                ff.searchable(),
                ff.clearable(),
                ff.showSelectAll(),
                ff.maxSelectable(),
                ff.optionsSource(),
                ff.dependsOn(),
                ff.valueKey(),
                ff.labelKey(),
                ff.optionLayout(),
                ff.allowCustomValue(),
                ff.debounceTime(),
                staticOptions.size(),
                staticOptions
        );
    }

    private FileUploadConfig buildFileUploadConfig(FormField ff, FieldType type) {
        if (type != FieldType.FILE) return null;

        return new FileUploadConfig(
                ff.multipleFiles(),
                ff.acceptedTypes(),
                ff.maxFileSize(),
                ff.uploadUrl()
        );
    }

    private Map<String, Object> buildCustomConditions(FormConditional fc, FormField ff) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (fc != null) {
            if (!fc.visibleWhen().isBlank()) map.put("visibleWhen", fc.visibleWhen());
            if (!fc.enabledWhen().isBlank()) map.put("enabledWhen", fc.enabledWhen());
            if (!fc.requiredWhen().isBlank()) map.put("requiredWhen", fc.requiredWhen());

            for (eu.isygoit.annotation.Condition c : fc.value()) {
                map.put(c.key(), c.value());
            }
        }
        return map;
    }

    private ListConfig buildListConfig(Field field, FieldType type) {
        FormList formList = field.getAnnotation(FormList.class);
        if (formList == null || (type != FieldType.LIST && type != FieldType.SET && type != FieldType.TABLE)) {
            return null;
        }
        return new ListConfig(
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
        if ("org.springframework.web.multipart.MultipartFile".equals(javaType.getName()) ||
                java.io.File.class.equals(javaType))
            return FieldType.FILE;
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

        // Options mapping
        map.put("multiple", ff.multiple());
        map.put("searchable", ff.searchable());
        map.put("clearable", ff.clearable());
        map.put("showSelectAll", ff.showSelectAll());
        map.put("maxSelectable", ff.maxSelectable());

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