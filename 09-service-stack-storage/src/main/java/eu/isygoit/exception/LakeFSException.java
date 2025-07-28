package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Lake fs exception.
 */
@MsgLocale("lakefs.exception")
public class LakeFSException extends ManagedException {

    /**
     * Instantiates a new Lake fs exception.
     *
     * @param message the message
     */
    public LakeFSException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Lake fs exception.
     *
     * @param throwable the throwable
     */
    public LakeFSException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Lake fs exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public LakeFSException(String message, Throwable cause) {
        super(message, cause);
    }
}
