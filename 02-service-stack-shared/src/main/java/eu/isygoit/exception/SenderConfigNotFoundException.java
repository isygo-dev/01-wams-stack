package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Sender config not found exception.
 */
@MsgLocale("sender.config.not.found.exception")
public class SenderConfigNotFoundException extends ManagedException {

    /**
     * Instantiates a new Sender config not found exception.
     *
     * @param message the message
     */
    public SenderConfigNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Sender config not found exception.
     *
     * @param throwable the throwable
     */
    public SenderConfigNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Sender config not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public SenderConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
