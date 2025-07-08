package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Linked file service not defined exception.
 */
@MsgLocale(value = "lnk.file.service.null.exception")
public class LinkedFileServiceNullException extends ManagedException {

    /**
     * Instantiates a new Linked file service not defined exception.
     *
     * @param message the message
     */
    public LinkedFileServiceNullException(String message) {
        super(message);
    }
}
