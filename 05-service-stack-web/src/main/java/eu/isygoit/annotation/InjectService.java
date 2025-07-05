package eu.isygoit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Ctrl service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectService {
    /**
     * Value class.
     *
     * @return the class
     */
    Class<?> value(); // service class
}
