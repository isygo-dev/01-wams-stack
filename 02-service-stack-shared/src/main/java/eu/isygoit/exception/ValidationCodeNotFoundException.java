package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Validation code not found exception.
 */
@MsgLocale(value = "validation.code.not.found.exception")
public class ValidationCodeNotFoundException extends ManagedException {

    /**
     * Instantiates a new Validation code not found exception.
     *
     * @param message the message
     */
    public ValidationCodeNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Validation code not found exception.
     *
     * @param throwable the throwable
     */
    public ValidationCodeNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Validation code not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ValidationCodeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
