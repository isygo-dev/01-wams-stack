package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Validation code expired exception.
 */
@MsgLocale(value = "validation.code.expired.exception")
public class ValidationCodeExpiredException extends ManagedException {

    /**
     * Instantiates a new Validation code expired exception.
     *
     * @param message the message
     */
    public ValidationCodeExpiredException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Validation code expired exception.
     *
     * @param throwable the throwable
     */
    public ValidationCodeExpiredException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Validation code expired exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ValidationCodeExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
