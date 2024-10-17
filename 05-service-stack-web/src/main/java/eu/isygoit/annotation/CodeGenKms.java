package eu.isygoit.annotation;

import eu.isygoit.service.IRemoteNextCodeService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Code gen kms.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CodeGenKms {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends IRemoteNextCodeService> value();
}
