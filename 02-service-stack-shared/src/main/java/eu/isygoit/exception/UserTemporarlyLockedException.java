package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type User temporarly locked exception.
 */
@MsgLocale(value = "user.temporarly.locked.exception")
public class UserTemporarlyLockedException extends ManagedException {

    /**
     * Instantiates a new User temporarly locked exception.
     *
     * @param message the message
     */
    public UserTemporarlyLockedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new User temporarly locked exception.
     *
     * @param throwable the throwable
     */
    public UserTemporarlyLockedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new User temporarly locked exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UserTemporarlyLockedException(String message, Throwable cause) {
        super(message, cause);
    }
}
