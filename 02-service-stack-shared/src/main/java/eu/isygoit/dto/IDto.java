package eu.isygoit.dto;

import java.io.Serializable;

/**
 * The interface Dto.
 *
 * @param <T> the type parameter
 */
public interface IDto<T extends Serializable> extends Serializable {

    /**
     * Gets section name.
     *
     * @return the section name
     */
    String getSectionName();

    /**
     * Is empty boolean.
     *
     * @return the boolean
     */
    boolean isEmpty();
}
