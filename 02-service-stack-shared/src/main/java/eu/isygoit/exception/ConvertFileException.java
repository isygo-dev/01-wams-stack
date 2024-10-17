package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Convert file exception.
 */
@MsgLocale("convert.file.exception")
public class ConvertFileException extends ManagedException {

    /**
     * Instantiates a new Convert file exception.
     *
     * @param message the message
     */
    public ConvertFileException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Convert file exception.
     *
     * @param throwable the throwable
     */
    public ConvertFileException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Convert file exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ConvertFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
