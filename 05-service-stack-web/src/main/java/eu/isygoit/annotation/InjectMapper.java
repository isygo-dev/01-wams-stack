package eu.isygoit.annotation;

import eu.isygoit.mapper.EntityMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject mappers into a REST controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectMapper {

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
}
