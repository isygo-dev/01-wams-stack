package eu.isygoit.annotation;

import eu.isygoit.model.json.JsonBasedEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsonEntity {

    Class<? extends JsonBasedEntity> value();
}
