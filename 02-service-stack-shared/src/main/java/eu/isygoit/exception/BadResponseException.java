package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Bad response exception.
 */
@MsgLocale(value = "bad.response.exception")
public class BadResponseException extends ManagedException {

    /**
     * Instantiates a new Bad response exception.
     *
     * @param message the message
     */
    public BadResponseException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Bad response exception.
     *
     * @param throwable the throwable
     */
    public BadResponseException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Bad response exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BadResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
