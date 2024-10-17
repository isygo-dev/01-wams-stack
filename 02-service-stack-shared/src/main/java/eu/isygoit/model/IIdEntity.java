package eu.isygoit.model;

import java.io.Serializable;

/**
 * The interface Id entity.
 *
 * @param <T> the type parameter
 */
public interface IIdEntity<T> extends Serializable {
    /**
     * Gets id.
     *
     * @return the id
     */
    T getId();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(T id);
}
