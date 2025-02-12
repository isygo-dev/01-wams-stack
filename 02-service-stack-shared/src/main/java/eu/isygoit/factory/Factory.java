package eu.isygoit.factory;

/**
 * The interface Factory.
 *
 * @param <E> the type parameter
 */
public interface Factory<E> {
    /**
     * Instance t.
     *
     * @return the t
     */
    E instance();

    /**
     * New instance t.
     *
     * @return the t
     */
    E newInstance();
}
