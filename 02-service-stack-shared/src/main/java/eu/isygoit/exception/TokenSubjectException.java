package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

@MsgLocale("token.subject.exception")
public class TokenSubjectException extends ManagedException {

    /**
     * Instantiates a new Token config not found exception.
     *
     * @param s the s
     */
    public TokenSubjectException(String s) {
        super(s);
    }

    /**
     * Instantiates a new Alias not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TokenSubjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
