package eu.isygoit.annotation;

import org.springframework.data.repository.Repository;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject a repository into a service.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface InjectRepository {
    /**
     * The repository class.
     *
     * @return the class
     */
    Class<? extends Repository> value();
}
