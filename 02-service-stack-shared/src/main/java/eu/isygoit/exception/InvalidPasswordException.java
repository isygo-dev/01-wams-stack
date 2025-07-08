package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Invalid password exception.
 */
@MsgLocale(value = "invalid.password.exception")
public class InvalidPasswordException extends ManagedException {
    /**
     * Instantiates a new Invalid password exception.
     *
     * @param s the s
     */
    public InvalidPasswordException(String s) {
    }
}
