package eu.isygoit.model;

/**
 * The interface Statable.
 *
 * @param <S> the type parameter
 */
public interface IStatusAssignable<S> {

    /**
     * Gets state.
     *
     * @return the state
     */
    S getState();

    /**
     * Sets state.
     *
     * @param state the state
     */
    void setState(S state);
}

