package eu.isygoit.model;

import java.io.Serializable;

/**
 * The interface Id entity.
 *
 * @param <I> the type parameter
 */
public interface IIdAssignable<I> extends Serializable {

    static final long serialVersionUID = 1L; // Use a fixed version

    /**
     * Gets id.
     *
     * @return the id
     */
    I getId();

    /**
     * Sets id.
     *
     * @param id the id
     */
    void setId(I id);
}
