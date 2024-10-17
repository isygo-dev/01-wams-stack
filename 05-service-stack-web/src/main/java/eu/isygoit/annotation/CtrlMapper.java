package eu.isygoit.annotation;

import eu.isygoit.mapper.EntityMapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Ctrl mapper.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CtrlMapper {

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
}
