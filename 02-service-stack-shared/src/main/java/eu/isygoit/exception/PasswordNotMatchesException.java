package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Password not matches exception.
 */
@MsgLocale("invalid.password.exception")
public class PasswordNotMatchesException extends ManagedException {

    /**
     * Instantiates a new Password not matches exception.
     *
     * @param message the message
     */
    public PasswordNotMatchesException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Password not matches exception.
     *
     * @param throwable the throwable
     */
    public PasswordNotMatchesException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Password not matches exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PasswordNotMatchesException(String message, Throwable cause) {
        super(message, cause);
    }
}
