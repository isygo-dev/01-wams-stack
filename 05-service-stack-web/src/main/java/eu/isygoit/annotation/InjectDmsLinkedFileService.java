package eu.isygoit.annotation;

import eu.isygoit.com.rest.api.ILinkedFileApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a DMS linked file service into a REST controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectDmsLinkedFileService {
    /**
     * The linked file service class.
     *
     * @return the class
     */
    Class<? extends ILinkedFileApi> value();
}
