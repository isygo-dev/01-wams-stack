package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Invalid credentials exception.
 */
@MsgLocale("invalid.credentials.exception")
public class InvalidCredentialsException extends ManagedException {

    /**
     * Instantiates a new Invalid credentials exception.
     *
     * @param message the message
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Invalid credentials exception.
     *
     * @param throwable the throwable
     */
    public InvalidCredentialsException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Invalid credentials exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
