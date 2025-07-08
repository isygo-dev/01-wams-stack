package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Unknown exception.
 */
@MsgLocale(value = "unknown.exception")
public class UnknownException extends ManagedException {

    /**
     * Instantiates a new Unknown exception.
     *
     * @param message the message
     */
    public UnknownException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Unknown exception.
     *
     * @param throwable the throwable
     */
    public UnknownException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Unknown exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}
