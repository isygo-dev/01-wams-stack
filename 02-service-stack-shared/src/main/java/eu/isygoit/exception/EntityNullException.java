package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Linked file api not defined exception.
 */
@MsgLocale(value = "entity.null.exception")
public class EntityNullException extends ManagedException {

    /**
     * Instantiates a new Linked file api not defined exception.
     *
     * @param message the message
     */
    public EntityNullException(String message) {
        super(message);
    }
}
