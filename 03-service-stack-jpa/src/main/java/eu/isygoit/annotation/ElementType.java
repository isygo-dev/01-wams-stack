package eu.isygoit.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the stable storage key used to discriminate a {@link eu.isygoit.model.json.JsonElement}
 * implementation within the shared JSON entity table.
 *
 * <p>The value must be unique across all {@code JsonElement} types persisted to the same table.
 * Convention: SCREAMING_SNAKE_CASE — e.g. {@code "USER_LOGIN"}, {@code "ORDER_PLACED"}.
 *
 * <p>If this annotation is absent the framework falls back to the simple class name
 * in upper case, which is collision-prone when two classes share the same simple name
 * across different packages.
 *
 * <p>Keeping the annotation value stable across class renames means stored data is
 * never orphaned — changing the class name does not invalidate existing rows.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.TYPE)
public @interface ElementType {

    /**
     * The stable, unique key for this element type.
     *
     * @return the element type key (SCREAMING_SNAKE_CASE)
     */
    String value();
}