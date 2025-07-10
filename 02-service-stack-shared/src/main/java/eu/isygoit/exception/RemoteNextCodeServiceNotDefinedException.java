package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Remote next code api not defined exception.
 */
@MsgLocale(value = "remote.ncs.not.defined.exception")
public class RemoteNextCodeServiceNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Remote next code api not defined exception.
     *
     * @param message the message
     */
    public RemoteNextCodeServiceNotDefinedException(String message) {
        super(message);
    }
}
