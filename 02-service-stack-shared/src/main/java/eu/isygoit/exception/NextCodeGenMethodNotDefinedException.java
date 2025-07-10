package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Next code api not defined exception.
 */
@MsgLocale(value = "next.code.gen.method.not.defined.exception")
public class NextCodeGenMethodNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Next code api not defined exception.
     *
     * @param message the message
     */
    public NextCodeGenMethodNotDefinedException(String message) {
        super(message);
    }
}
