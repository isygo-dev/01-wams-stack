package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Unsuported auth type exception.
 */
@MsgLocale(value = "unsupported.auth.type.exception")
public class UnsuportedAuthTypeException extends ManagedException {

    /**
     * Instantiates a new Unsuported auth type exception.
     *
     * @param message the message
     */
    public UnsuportedAuthTypeException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Unsuported auth type exception.
     *
     * @param throwable the throwable
     */
    public UnsuportedAuthTypeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Unsuported auth type exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UnsuportedAuthTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
