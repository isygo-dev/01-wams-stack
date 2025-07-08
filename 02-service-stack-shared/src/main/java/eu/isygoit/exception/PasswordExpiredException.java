package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Password expired exception.
 */
@MsgLocale(value = "password.expired.exception")
public class PasswordExpiredException extends ManagedException {

    /**
     * Instantiates a new Password expired exception.
     *
     * @param message the message
     */
    public PasswordExpiredException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Password expired exception.
     *
     * @param throwable the throwable
     */
    public PasswordExpiredException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Password expired exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PasswordExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
