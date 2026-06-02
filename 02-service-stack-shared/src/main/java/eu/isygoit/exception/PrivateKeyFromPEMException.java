package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type App parameter not found exception.
 */
@MsgLocale(value = "private.key.from.pem.exception")
public class PrivateKeyFromPEMException extends ManagedException {

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     */
    public PrivateKeyFromPEMException(String message) {
        super(message);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param throwable the throwable
     */
    public PrivateKeyFromPEMException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PrivateKeyFromPEMException(String message, Throwable cause) {
        super(message, cause);
    }
}
