package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Next code api not defined exception.
 */
@MsgLocale(value = "next.code.api.not.defined.exception")
public class NextCodeServiceNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Next code api not defined exception.
     *
     * @param message the message
     */
    public NextCodeServiceNotDefinedException(String message) {
        super(message);
    }
}
