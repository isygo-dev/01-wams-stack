package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Empty list exception.
 */
@MsgLocale("empty.list.exception")
public class EmptyListException extends ManagedException {

    /**
     * Instantiates a new Empty list exception.
     */
    public EmptyListException() {
    }

    /**
     * Instantiates a new Empty list exception.
     *
     * @param message the message
     */
    public EmptyListException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Empty list exception.
     *
     * @param cause the cause
     */
    public EmptyListException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Empty list exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EmptyListException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Empty list exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public EmptyListException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
