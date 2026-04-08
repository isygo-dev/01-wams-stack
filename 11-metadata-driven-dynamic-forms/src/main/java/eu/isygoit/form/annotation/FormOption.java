package eu.isygoit.form.annotation;

import java.lang.annotation.*;

@Repeatable(FormOptions.class)   // ← This makes it repeatable
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormOption {

    String value();

    String label();

    String description() default "";

    boolean disabled() default false;

    String group() default "";

    String icon() default "";

    String color() default "";

    int order() default 0;
}