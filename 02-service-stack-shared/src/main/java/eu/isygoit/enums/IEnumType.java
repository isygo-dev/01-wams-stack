package eu.isygoit.enums;

import java.io.Serializable;

/**
 * The interface Enum type.
 *
 * @param <T> the type parameter
 */
public interface IEnumType<T> extends Serializable {

    /**
     * Name string.
     *
     * @return the string
     */
    String name();
}
