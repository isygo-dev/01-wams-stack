package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Unsupported file type exception.
 */
@MsgLocale(value = "unsupported.file.type.exception")
public class UnsupportedFileTypeException extends ManagedException {

    /**
     * Instantiates a new Unsupported file type exception.
     *
     * @param message the message
     */
    public UnsupportedFileTypeException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Unsupported file type exception.
     *
     * @param throwable the throwable
     */
    public UnsupportedFileTypeException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Unsupported file type exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UnsupportedFileTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
