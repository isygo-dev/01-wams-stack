package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Incremental config not found exception.
 */
@MsgLocale(value = "incremental.config.not.found.exception")
public class IncrementalConfigNotFoundException extends ManagedException {

    /**
     * Instantiates a new Incremental config not found exception.
     *
     * @param message the message
     */
    public IncrementalConfigNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Incremental config not found exception.
     *
     * @param throwable the throwable
     */
    public IncrementalConfigNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Incremental config not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public IncrementalConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
