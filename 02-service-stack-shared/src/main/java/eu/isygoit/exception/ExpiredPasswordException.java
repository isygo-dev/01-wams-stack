package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Expired password exception.
 */
@MsgLocale("expired.password.exception")
public class ExpiredPasswordException extends ManagedException {

    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Expired password exception.
     *
     * @param message the message
     */
    public ExpiredPasswordException(String message) {
        super(message);
    }

}
