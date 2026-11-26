package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

@MsgLocale("token.build.exception")
public class TokenBuildException extends ManagedException {

    /**
     * Instantiates a new Token config not found exception.
     *
     * @param s the s
     */
    public TokenBuildException(String s) {
        super(s);
    }

    /**
     * Instantiates a new Alias not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
