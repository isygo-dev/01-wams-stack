package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Token expired exception.
 */
@MsgLocale(value = "token.expired.exception")
public class TokenExpiredException extends ManagedException {

    /**
     * Instantiates a new Token expired exception.
     *
     * @param message the message
     */
    public TokenExpiredException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Token expired exception.
     *
     * @param throwable the throwable
     */
    public TokenExpiredException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Token expired exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
