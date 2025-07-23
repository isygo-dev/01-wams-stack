package eu.isygoit.annotation;

import eu.isygoit.model.json.JsonBasedEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Json entity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsonEntity {

    /**
     * Value class.
     *
     * @return the class
     */
    Class<? extends JsonBasedEntity> value();
}
