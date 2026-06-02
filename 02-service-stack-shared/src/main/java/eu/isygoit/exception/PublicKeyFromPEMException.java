package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type App parameter not found exception.
 */
@MsgLocale(value = "public.key.from.pem.exception")
public class PublicKeyFromPEMException extends ManagedException {

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     */
    public PublicKeyFromPEMException(String message) {
        super(message);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param throwable the throwable
     */
    public PublicKeyFromPEMException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public PublicKeyFromPEMException(String message, Throwable cause) {
        super(message, cause);
    }
}
