package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Password not valid exception.
 */
@MsgLocale("password.reset.token.exception")
public class PasswordNotValidException extends ManagedException {

    /**
     * Instantiates a new Password not valid exception.
     *
     * @param message the message
     */
    public PasswordNotValidException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Password not valid exception.
     *
     * @param throwable the throwable
     */
    public PasswordNotValidException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Password not valid exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PasswordNotValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
