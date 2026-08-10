package eu.isygoit.annotation;

import eu.isygoit.com.rest.service.ICrudServiceUtils;
import eu.isygoit.exception.handler.IExceptionHandler;
import eu.isygoit.mapper.EntityMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Consolidated annotation for REST controller configuration.
 * Used to define service, mappers, and exception handler for a controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RestConfiguration {

    /**
     * Service class to be injected.
     */
    Class<? extends ICrudServiceUtils> service() default ICrudServiceUtils.class;

    /**
     * Full DTO entity mapper class.
     */
    Class<? extends EntityMapper> mapper() default EntityMapper.class;

    /**
     * Minimal DTO entity mapper class.
     */
    Class<? extends EntityMapper> minMapper() default EntityMapper.class;

    /**
     * Exception handler class.
     */
    Class<? extends IExceptionHandler> exceptionHandler() default IExceptionHandler.class;
}
