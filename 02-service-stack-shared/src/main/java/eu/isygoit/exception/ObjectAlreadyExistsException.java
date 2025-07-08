package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Bad argument exception.
 */
@MsgLocale(value = "object.already.exists.exception")
public class ObjectAlreadyExistsException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public ObjectAlreadyExistsException(String message) {
        super(message);
    }
}
