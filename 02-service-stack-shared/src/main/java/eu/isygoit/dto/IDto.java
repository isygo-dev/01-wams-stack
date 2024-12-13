package eu.isygoit.dto;

import java.io.Serializable;

/**
 * The interface Dto.
 */
public interface IDto extends Serializable {

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
