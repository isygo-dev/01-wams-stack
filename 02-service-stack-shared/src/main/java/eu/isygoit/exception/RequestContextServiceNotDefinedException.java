package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type App parameter not found exception.
 */
@MsgLocale(value = "request.context.service.not.defined.exception")
public class RequestContextServiceNotDefinedException extends ManagedException {

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     */
    public RequestContextServiceNotDefinedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param throwable the throwable
     */
    public RequestContextServiceNotDefinedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public RequestContextServiceNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}
