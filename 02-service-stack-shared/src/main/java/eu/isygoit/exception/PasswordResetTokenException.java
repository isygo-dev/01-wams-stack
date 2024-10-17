package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Password reset token exception.
 */
@MsgLocale("password.reset.token.exception")
public class PasswordResetTokenException extends ManagedException {

    /**
     * Instantiates a new Password reset token exception.
     *
     * @param message the message
     */
    public PasswordResetTokenException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Password reset token exception.
     *
     * @param throwable the throwable
     */
    public PasswordResetTokenException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Password reset token exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PasswordResetTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
