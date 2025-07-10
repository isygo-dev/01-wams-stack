package eu.isygoit.storage.exception;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.exception.ManagedException;


/**
 * The type Min io object exception.
 */
@MsgLocale("copy.get.object.exception")
public class CopyObjectException extends ManagedException {

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     */
    public CopyObjectException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param throwable the throwable
     */
    public CopyObjectException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CopyObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
