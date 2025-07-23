package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Object not found exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "empty.file.exception")
public class EmptyFileException extends ManagedException {

    /**
     * Instantiates a new Object not found exception.
     *
     * @param message the message
     */
    public EmptyFileException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Object not found exception.
     *
     * @param throwable the throwable
     */
    public EmptyFileException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Object not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EmptyFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
