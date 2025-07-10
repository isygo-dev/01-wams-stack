package eu.isygoit.storage.exception;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.exception.ManagedException;


/**
 * The type Min io object exception.
 */
@MsgLocale("open.get.object.exception")
public class OpenIOObjectException extends ManagedException {

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     */
    public OpenIOObjectException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param throwable the throwable
     */
    public OpenIOObjectException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public OpenIOObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
