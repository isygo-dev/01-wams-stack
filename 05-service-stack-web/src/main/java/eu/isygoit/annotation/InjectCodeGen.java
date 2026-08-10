package eu.isygoit.annotation;

import eu.isygoit.service.nextCode.ICodeGeneratorService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a code generator service into a REST controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectCodeGen {
    /**
     * The code generator service class.
     *
     * @return the class
     */
    Class<? extends ICodeGeneratorService> value();
}
