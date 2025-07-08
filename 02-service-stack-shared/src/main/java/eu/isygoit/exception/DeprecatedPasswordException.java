package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Deprecated password exception.
 */
@MsgLocale(value = "deprecated.password.exception")
public class DeprecatedPasswordException extends ManagedException {

    /**
     * The constant serialVersionUID.
     */
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
