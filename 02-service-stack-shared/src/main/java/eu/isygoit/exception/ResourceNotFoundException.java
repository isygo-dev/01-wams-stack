package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Resource not found exception.
 */
@MsgLocale("resource.not.found.exception")
public class ResourceNotFoundException extends ManagedException {

    /**
     * Instantiates a new Resource not found exception.
     *
     * @param message the message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Resource not found exception.
     *
     * @param throwable the throwable
     */
    public ResourceNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Resource not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
