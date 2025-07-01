package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Bad argument exception.
 */
@MsgLocale("null.identifier.exception")
public class NullIdentifierException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public NullIdentifierException(String message) {
        super(message);
    }
}
