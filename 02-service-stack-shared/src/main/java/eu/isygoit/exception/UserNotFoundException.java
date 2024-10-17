package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type User not found exception.
 */
@MsgLocale("user.not.found.exception")
public class UserNotFoundException extends ManagedException {

    /**
     * Instantiates a new User not found exception.
     *
     * @param message the message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new User not found exception.
     *
     * @param throwable the throwable
     */
    public UserNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new User not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
