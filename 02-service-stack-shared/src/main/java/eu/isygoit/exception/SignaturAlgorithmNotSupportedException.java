package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

@MsgLocale("signatur.algorithm.not.supported.exception")
public class SignaturAlgorithmNotSupportedException extends ManagedException {

    /**
     * Instantiates a new Token config not found exception.
     *
     * @param s the s
     */
    public SignaturAlgorithmNotSupportedException(String s) {
        super(s);
    }

    /**
     * Instantiates a new Alias not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public SignaturAlgorithmNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
