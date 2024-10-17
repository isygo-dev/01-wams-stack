package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Authentification exception.
 */
@MsgLocale("authentification.exception")
public class AuthentificationException extends ManagedException {

    /**
     * Instantiates a new Authentification exception.
     *
     * @param message the message
     */
    public AuthentificationException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Authentification exception.
     *
     * @param throwable the throwable
     */
    public AuthentificationException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Authentification exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public AuthentificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
