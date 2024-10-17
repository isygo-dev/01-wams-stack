package eu.isygoit.annotation;

import eu.isygoit.service.nextCode.INextCodeService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Code gen local.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CodeGenLocal {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends INextCodeService> value();
}
