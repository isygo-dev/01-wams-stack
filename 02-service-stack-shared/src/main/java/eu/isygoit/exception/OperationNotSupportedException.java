package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Operation not allowed exception.
 */
@MsgLocale(value = "operation.not.supported.exception")
public class OperationNotSupportedException extends ManagedException {

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     */
    public OperationNotSupportedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param throwable the throwable
     */
    public OperationNotSupportedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public OperationNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
