package eu.isygoit.annotation;

import eu.isygoit.com.rest.service.ICodifiableService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Ctrl service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CtrlService {
    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends ICodifiableService> value(); // service class
}
