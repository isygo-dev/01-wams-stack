package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type App parameter not found exception.
 */
@MsgLocale("bad.fiels.name.exception")
public class BadFieldNameException extends ManagedException {

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     */
    public BadFieldNameException(String message) {
        super(message);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param throwable the throwable
     */
    public BadFieldNameException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BadFieldNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
