package eu.isygoit.annotation;

import java.lang.annotation.*;

@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
    String key();
    String value();
}