package eu.isygoit.enums;

import java.io.Serializable;

/**
 * The interface Enum type.
 *
 * @param <E> the type parameter
 */
public interface IEnumType<E> extends Serializable {

    /**
     * Name string.
     *
     * @return the string
     */
    String name();
}
