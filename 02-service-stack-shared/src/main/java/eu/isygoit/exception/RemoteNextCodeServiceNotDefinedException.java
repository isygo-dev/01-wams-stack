package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Remote next code service not defined exception.
 */
@MsgLocale("remote.ncs.not.defined.exception")
public class RemoteNextCodeServiceNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Remote next code service not defined exception.
     *
     * @param message the message
     */
    public RemoteNextCodeServiceNotDefinedException(String message) {
        super(message);
    }
}
