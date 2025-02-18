package eu.isygoit.annotation;

import eu.isygoit.com.rest.api.IDmsLinkedFileService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Dms link file service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DmsLinkFileService {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends IDmsLinkedFileService> value();
}
