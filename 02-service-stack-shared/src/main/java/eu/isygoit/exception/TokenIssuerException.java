package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

@MsgLocale("token.issuer.exception")
public class TokenIssuerException extends ManagedException {

    /**
     * Instantiates a new Token config not found exception.
     *
     * @param s the s
     */
    public TokenIssuerException(String s) {
        super(s);
    }

    /**
     * Instantiates a new Alias not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenIssuerException(String message, Throwable cause) {
        super(message, cause);
    }
}
