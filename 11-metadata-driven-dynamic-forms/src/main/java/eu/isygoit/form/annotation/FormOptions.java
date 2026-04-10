package eu.isygoit.form.annotation;

import java.lang.annotation.*;

// Container annotation for repeatability
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FormOptions {
    FormOption[] value();
}