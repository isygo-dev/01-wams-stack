package eu.isygoit.annotation;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.handler.IExceptionHandler;
import eu.isygoit.mapper.EntityMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Combined annotation to inject mappers, services, and exception handlers into a REST controller.
 * @deprecated Use {@link RestConfiguration} for a more comprehensive configuration.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectMapperAndService {
    /**
     * The exception handler class.
     *
     * @return the class
     */
    Class<? extends IExceptionHandler> handler();

    /**
     * Full DTO entity mapper class.
     *
     * @return the class
     */
    Class<? extends EntityMapper> mapper();

    /**
     * Minimal DTO entity mapper class.
     *
     * @return the class
     */
    Class<? extends EntityMapper> minMapper();

    /**
     * The service class.
     *
     * @return the class
     */
    Class<? extends ICrudServiceUtils> service();
}
