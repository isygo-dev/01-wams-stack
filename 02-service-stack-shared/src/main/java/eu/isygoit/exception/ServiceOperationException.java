package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Operation not allowed exception.
 */
@MsgLocale(value = "api.operation.exception")
public class ServiceOperationException extends ManagedException {

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     */
    public ServiceOperationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param throwable the throwable
     */
    public ServiceOperationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ServiceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
