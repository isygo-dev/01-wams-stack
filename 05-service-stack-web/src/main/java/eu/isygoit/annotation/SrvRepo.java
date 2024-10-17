package eu.isygoit.annotation;

import org.springframework.data.repository.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Srv repo.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SrvRepo {
    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends Repository> value();
}
