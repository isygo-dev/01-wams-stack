package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

@MsgLocale("token.audience.exception")
public class TokenAudienceException extends ManagedException {

    /**
     * Instantiates a new Token config not found exception.
     *
     * @param s the s
     */
    public TokenAudienceException(String s) {
        super(s);
    }

    /**
     * Instantiates a new Alias not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenAudienceException(String message, Throwable cause) {
        super(message, cause);
    }
}
