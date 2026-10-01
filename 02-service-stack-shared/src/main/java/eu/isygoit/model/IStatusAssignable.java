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
     * @return the status
     */
    S getStatus();

    /**
     * Sets status.
     *
     * @param status the status
     */
    void setStatus(S status);
}

