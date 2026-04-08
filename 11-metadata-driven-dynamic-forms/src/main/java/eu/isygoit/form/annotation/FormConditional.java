package eu.isygoit.form.annotation;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormConditional {

    String visibleWhen() default "";

    String enabledWhen() default "";

    String requiredWhen() default "";

    // Custom key-value pairs that will go into customConditions map
    Condition[] value() default {};
}