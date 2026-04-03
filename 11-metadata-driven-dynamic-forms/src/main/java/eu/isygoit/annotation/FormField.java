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
    String display() default "input";   // "input" | "output"
    boolean disabled() default false;
    boolean readonly() default false;

    // Validation
    String pattern() default "";
    int minLength() default -1;
    int maxLength() default -1;
    double minValue() default Double.MIN_VALUE;
    double maxValue() default Double.MAX_VALUE;

    // Formatting & Masking
    String format() default "";                    // currency, percentage, phone...
    String mask() default "";
    boolean useMask() default false;

    String prefix() default "";
    String suffix() default "";
    String thousandSeparator() default ",";
    String decimalSeparator() default ".";

    // UI Specific
    int rows() default 3;                          // for TEXTAREA
    String defaultValue() default "";              // as String (can be parsed later)

    // Conditional Logic (simple string expressions for now)
    String visibleWhen() default "";
    String enabledWhen() default "";
    String requiredWhen() default "";
}