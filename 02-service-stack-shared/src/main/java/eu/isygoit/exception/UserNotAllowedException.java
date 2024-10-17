package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type User not allowed exception.
 */
@MsgLocale("user.not.allowed")
public class UserNotAllowedException extends ManagedException {

    /**
     * Instantiates a new User not allowed exception.
     *
     * @param message the message
     */
    public UserNotAllowedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new User not allowed exception.
     *
     * @param throwable the throwable
     */
    public UserNotAllowedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new User not allowed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UserNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
