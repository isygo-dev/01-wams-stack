package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Invalid activation code exception.
 */
@MsgLocale("invalid.activation.code.exception")
public class InvalidActivationCodeException extends ManagedException {
    /**
     * Instantiates a new Invalid activation code exception.
     */
    public InvalidActivationCodeException() {
        super();
    }

    /**
     * Instantiates a new Invalid activation code exception.
     *
     * @param message the message
     */
    public InvalidActivationCodeException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Invalid activation code exception.
     *
     * @param cause the cause
     */
    public InvalidActivationCodeException(Throwable cause) {
        super(cause);
    }
}
