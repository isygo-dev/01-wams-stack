package eu.isygoit.factory;

/**
 * The interface Factory.
 *
 * @param <T> the type parameter
 */
public interface Factory<T> {
    /**
     * Instance t.
     *
     * @return the t
     */
    T instance();

    /**
     * New instance t.
     *
     * @return the t
     */
    T newInstance();
}
