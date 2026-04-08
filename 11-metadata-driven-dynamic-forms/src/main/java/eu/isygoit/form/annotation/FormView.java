package eu.isygoit.form.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormView {
    String name();

    String title() default "";

    String description() default "";

    String version() default "1.0";

    boolean cacheable() default true;
}