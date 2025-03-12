package eu.isygoit.exception;
/**
 * @author isygoit
 */

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Invalid field exception.
 */
@MsgLocale("invalid.field.exception")
public class InvalidFieldException extends ManagedException {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Invalid field exception.
     */
    public InvalidFieldException() {
    }

    /**
     * Instantiates a new Invalid field exception.
     *
     * @param message the message
     */
    public InvalidFieldException(String message) {
        super(message);
    }

}
