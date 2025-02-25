package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Deprecated password exception.
 */
@MsgLocale("deprecated.password.exception")
public class DeprecatedPasswordException extends ManagedException {

    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Deprecated password exception.
     *
     * @param message the message
     */
    public DeprecatedPasswordException(String message) {
        super(message);
    }

}
