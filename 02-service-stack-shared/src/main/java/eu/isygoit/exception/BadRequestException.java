package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Bad request exception.
 */
@MsgLocale("bad.request.exception")
public class BadRequestException extends ManagedException {

    /**
     * Instantiates a new Bad request exception.
     *
     * @param message the message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Bad request exception.
     *
     * @param throwable the throwable
     */
    public BadRequestException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Bad request exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
