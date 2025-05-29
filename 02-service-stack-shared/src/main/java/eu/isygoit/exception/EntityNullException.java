package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Linked file service not defined exception.
 */
@MsgLocale("entity.null.exception")
public class EntityNullException extends ManagedException {

    /**
     * Instantiates a new Linked file service not defined exception.
     *
     * @param message the message
     */
    public EntityNullException(String message) {
        super(message);
    }
}
