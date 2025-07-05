package eu.isygoit.annotation;

import eu.isygoit.exception.handler.IExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Ctrl handler.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectExceptionHandler {
    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends IExceptionHandler> value(); // Data Exception Handler class
}
