package eu.isygoit.annotation;

import eu.isygoit.service.IRemoteNextCodeService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a remote code generator service (KMS) into a REST controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectCodeGenKms {
    /**
     * The remote code generator service class.
     *
     * @return the class
     */
    Class<? extends IRemoteNextCodeService> value();
}
