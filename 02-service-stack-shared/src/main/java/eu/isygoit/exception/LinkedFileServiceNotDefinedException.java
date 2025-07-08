package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Linked file service not defined exception.
 */
@MsgLocale(value = "lnk.file.service.not.defined.exception")
public class LinkedFileServiceNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Linked file service not defined exception.
     *
     * @param message the message
     */
    public LinkedFileServiceNotDefinedException(String message) {
        super(message);
    }
}
