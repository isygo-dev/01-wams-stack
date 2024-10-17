package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Exception handler not defined exception.
 */
@MsgLocale("exception.handler.not.defined.exception")
public class ExceptionHandlerNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Exception handler not defined exception.
     *
     * @param message the message
     */
    public ExceptionHandlerNotDefinedException(String message) {
        super(message);
    }
}
