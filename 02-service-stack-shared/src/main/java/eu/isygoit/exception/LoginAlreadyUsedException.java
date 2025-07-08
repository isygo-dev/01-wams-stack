package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Login already used exception.
 */
@MsgLocale(value = "login.already.used.exception")
public class LoginAlreadyUsedException extends ManagedException {

    /**
     * Instantiates a new Login already used exception.
     *
     * @param message the message
     */
    public LoginAlreadyUsedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Login already used exception.
     *
     * @param throwable the throwable
     */
    public LoginAlreadyUsedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Login already used exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public LoginAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
