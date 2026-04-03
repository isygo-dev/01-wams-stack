package eu.isygoit.annotation;

import eu.isygoit.domain.FieldType;
import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormField {

    String key() default "";

    String label() default "";

    String placeholder() default "";

    String helpText() default "";

    String tooltip() default "";

    FieldType type() default FieldType.AUTO;

    boolean required() default false;

    String requiredMessage() default "";

    int order() default Integer.MAX_VALUE;

    // Display & Behavior
    String display() default "input";
    boolean disabled() default false;
    boolean readonly() default false;

    // Validation
    String pattern() default "";
    int minLength() default -1;
    int maxLength() default -1;
    double minValue() default Double.MIN_VALUE;
    double maxValue() default Double.MAX_VALUE;

    // Formatting & Masking
    String format() default "";
    String mask() default "";
    boolean useMask() default false;

    String prefix() default "";
    String suffix() default "";
    String thousandSeparator() default ",";
    String decimalSeparator() default ".";

    // UI Specific
    int rows() default 3;
    String defaultValue() default "";

    // Conditional Logic
    String visibleWhen() default "";
    String enabledWhen() default "";
    String requiredWhen() default "";

    // ==================== RICH OPTIONS SUPPORT ====================
    boolean multiple() default false;
    boolean searchable() default false;
    boolean clearable() default true;
    boolean showSelectAll() default false;
    int maxSelectable() default Integer.MAX_VALUE;

    String optionsSource() default "static";     // static, enum, api, dependent
    String dependsOn() default "";               // for cascading dropdowns
    String valueKey() default "value";

    String optionLayout() default "dropdown";    // dropdown, inline, chips, cards

    // Autocomplete specific
    boolean allowCustomValue() default false;
    int debounceTime() default 300;

    // ==================== FILE UPLOAD SUPPORT ====================
    boolean multipleFiles() default false;
    String[] acceptedTypes() default {};         // e.g. ".pdf", "image/*", ".jpg,.png"
    long maxFileSize() default 10_485_760L;      // 10MB default
    String uploadUrl() default "";               // optional custom upload endpoint

    // ==================== i18n SUPPORT ====================
    String labelKey() default "";
    String placeholderKey() default "";
    String helpTextKey() default "";
    String tooltipKey() default "";
    String errorMessageKey() default "";
}