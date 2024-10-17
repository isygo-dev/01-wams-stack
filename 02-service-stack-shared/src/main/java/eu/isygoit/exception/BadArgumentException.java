package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Bad argument exception.
 */
@MsgLocale("bad.argument.exception")
public class BadArgumentException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public BadArgumentException(String message) {
        super(message);
    }
}
