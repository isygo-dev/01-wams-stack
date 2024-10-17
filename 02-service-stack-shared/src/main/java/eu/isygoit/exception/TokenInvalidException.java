package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Token invalid exception.
 */
@MsgLocale("invalid.token.exception")
public class TokenInvalidException extends ManagedException {

    /**
     * Instantiates a new Token invalid exception.
     *
     * @param message the message
     */
    public TokenInvalidException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Token invalid exception.
     *
     * @param throwable the throwable
     */
    public TokenInvalidException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Token invalid exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
