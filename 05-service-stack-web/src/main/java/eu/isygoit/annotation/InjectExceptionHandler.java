package eu.isygoit.annotation;

import eu.isygoit.exception.handler.IExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject an exception handler into a REST controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectExceptionHandler {
    /**
     * The exception handler class.
     *
     * @return the class
     */
    Class<? extends IExceptionHandler> value();
}
