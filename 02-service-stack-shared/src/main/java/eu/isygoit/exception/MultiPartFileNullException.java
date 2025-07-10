package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Linked file api not defined exception.
 */
@MsgLocale(value = "multipart.file.null.exception")
public class MultiPartFileNullException extends ManagedException {

    /**
     * Instantiates a new Linked file api not defined exception.
     *
     * @param message the message
     */
    public MultiPartFileNullException(String message) {
        super(message);
    }
}
