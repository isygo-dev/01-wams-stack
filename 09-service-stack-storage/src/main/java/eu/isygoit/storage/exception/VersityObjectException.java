package eu.isygoit.storage.exception;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.exception.ManagedException;


/**
 * The type Min io object exception.
 */
@MsgLocale("versity.get.object.exception")
public class VersityObjectException extends ManagedException {

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     */
    public VersityObjectException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param throwable the throwable
     */
    public VersityObjectException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public VersityObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
