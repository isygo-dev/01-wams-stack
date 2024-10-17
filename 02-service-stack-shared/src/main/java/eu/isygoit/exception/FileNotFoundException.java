package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type File not found exception.
 */
@MsgLocale("file.not.found.exception")
public class FileNotFoundException extends ManagedException {

    /**
     * Instantiates a new File not found exception.
     *
     * @param message the message
     */
    public FileNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new File not found exception.
     *
     * @param throwable the throwable
     */
    public FileNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new File not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public FileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
