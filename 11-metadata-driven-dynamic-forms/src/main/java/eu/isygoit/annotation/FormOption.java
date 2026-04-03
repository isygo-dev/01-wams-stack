package eu.isygoit.annotation;

import java.lang.annotation.*;

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