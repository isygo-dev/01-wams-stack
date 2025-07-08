package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Conflict exception.
 */
@MsgLocale(value = "conflict.exception")
public class ConflictException extends ManagedException {

    /**
     * Instantiates a new Conflict exception.
     *
     * @param message the message
     */
    public ConflictException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Conflict exception.
     *
     * @param throwable the throwable
     */
    public ConflictException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Conflict exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
