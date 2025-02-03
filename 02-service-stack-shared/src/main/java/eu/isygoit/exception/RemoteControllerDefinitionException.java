package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Remote encrypt definition exception.
 */
@MsgLocale("remote.encrypt.not.defined")
public class RemoteControllerDefinitionException extends ManagedException {

    /**
     * Instantiates a new Remote encrypt definition exception.
     *
     * @param message the message
     */
    public RemoteControllerDefinitionException(String message) {
        super(message);
    }
}
