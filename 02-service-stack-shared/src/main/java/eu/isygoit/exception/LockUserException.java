package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Lock user exception.
 */
@MsgLocale(value = "user.account.deactivation.denied")
public class LockUserException extends ManagedException {

    /**
     * Instantiates a new Lock user exception.
     *
     * @param message the message
     */
    public LockUserException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Lock user exception.
     *
     * @param throwable the throwable
     */
    public LockUserException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Lock user exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public LockUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
