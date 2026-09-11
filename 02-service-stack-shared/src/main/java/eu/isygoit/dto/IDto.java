package eu.isygoit.dto;

import java.io.Serializable;

/**
 * The interface Dto.
 */
public interface IDto extends Serializable {

    static final long serialVersionUID = 1L; // Use a fixed version

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
