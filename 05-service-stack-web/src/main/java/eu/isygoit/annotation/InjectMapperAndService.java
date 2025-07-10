package eu.isygoit.annotation;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.handler.IExceptionHandler;
import eu.isygoit.mapper.EntityMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Ctrl def.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectMapperAndService {
    /**
     * Handler class.
     *
     * @return the class
     */
    Class<? extends IExceptionHandler> handler(); // Data Exception Handler class

    /**
     * Mapper class.
     *
     * @return the class
     */
    Class<? extends EntityMapper> mapper(); // full dto / entity mapper class

    /**
     * Min mapper class.
     *
     * @return the class
     */
    Class<? extends EntityMapper> minMapper(); // min dto / entity mapper class

    /**
     * Service class.
     *
     * @return the class
     */
    Class<? extends ICrudServiceUtils> service(); // eu.isygoit.api class
}
