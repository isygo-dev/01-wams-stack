package eu.isygoit.exception;


import eu.isygoit.annotation.MsgLocale;


/**
 * The type Token not found exception.
 */
@MsgLocale(value = "token.not.found.exception")
public class TokenNotFoundException extends ManagedException {

    /**
     * Instantiates a new Token not found exception.
     *
     * @param message the message
     */
    public TokenNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Token not found exception.
     *
     * @param throwable the throwable
     */
    public TokenNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Token not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
