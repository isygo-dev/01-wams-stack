package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Unauthorized exception.
 */
@MsgLocale(value = "unauthorized.exception")
public class UnauthorizedException extends ManagedException {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Unauthorized exception.
     *
     * @param message the message
     */
    public UnauthorizedException(String message) {
        super(message);
    }

}
