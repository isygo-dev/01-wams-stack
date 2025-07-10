package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Linked file api not defined exception.
 */
@MsgLocale(value = "lnk.file.api.not.defined.exception")
public class LinkedFileServiceNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Linked file api not defined exception.
     *
     * @param message the message
     */
    public LinkedFileServiceNotDefinedException(String message) {
        super(message);
    }
}
