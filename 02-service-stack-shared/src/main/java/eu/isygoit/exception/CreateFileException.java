package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Create file exception.
 */
@MsgLocale(value = "create.file.exception")
public class CreateFileException extends ManagedException {

    /**
     * Instantiates a new Create file exception.
     *
     * @param message the message
     */
    public CreateFileException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Create file exception.
     *
     * @param throwable the throwable
     */
    public CreateFileException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Create file exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CreateFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
