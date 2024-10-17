package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type User already exists exception.
 */
@MsgLocale("user.already.exists.exception")
public class UserAlreadyExistsException extends ManagedException {

    /**
     * Instantiates a new User already exists exception.
     *
     * @param message the message
     */
    public UserAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Instantiates a new User already exists exception.
     *
     * @param throwable the throwable
     */
    public UserAlreadyExistsException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new User already exists exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
