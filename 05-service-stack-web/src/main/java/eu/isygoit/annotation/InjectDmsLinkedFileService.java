package eu.isygoit.annotation;

import eu.isygoit.com.rest.api.ILinkedFileApi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Dms link file service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectDmsLinkedFileService {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends ILinkedFileApi> value();
}
