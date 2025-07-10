package eu.isygoit.storage.exception;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.exception.ManagedException;


/**
 * The type Min io object exception.
 */
@MsgLocale("garage.get.object.exception")
public class GarageObjectException extends ManagedException {

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     */
    public GarageObjectException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param throwable the throwable
     */
    public GarageObjectException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public GarageObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
