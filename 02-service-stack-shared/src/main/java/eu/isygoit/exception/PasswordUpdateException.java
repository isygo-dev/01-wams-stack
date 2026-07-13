package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Password expired exception.
 */
@MsgLocale(value = "password.update.exception")
public class PasswordUpdateException extends ManagedException {

    /**
     * Instantiates a new Password expired exception.
     *
     * @param message the message
     */
    public PasswordUpdateException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Password expired exception.
     *
     * @param throwable the throwable
     */
    public PasswordUpdateException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Password expired exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PasswordUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
