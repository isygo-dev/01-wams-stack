package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Operation not allowed exception.
 */
@MsgLocale("operation.not.allowed.exception")
public class OperationNotAllowedException extends ManagedException {

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     */
    public OperationNotAllowedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param throwable the throwable
     */
    public OperationNotAllowedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public OperationNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
