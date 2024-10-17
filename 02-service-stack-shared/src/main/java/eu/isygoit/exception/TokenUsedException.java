package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Token used exception.
 */
@MsgLocale("token.expired.exception")
public class TokenUsedException extends ManagedException {

    /**
     * Instantiates a new Token used exception.
     *
     * @param message the message
     */
    public TokenUsedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Token used exception.
     *
     * @param throwable the throwable
     */
    public TokenUsedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Token used exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenUsedException(String message, Throwable cause) {
        super(message, cause);
    }
}
