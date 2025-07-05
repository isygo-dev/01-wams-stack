package eu.isygoit.annotation;

import eu.isygoit.service.nextCode.ICodeGeneratorService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Code gen local.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectCodeGen {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends ICodeGeneratorService> value();
}
