package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Object not found exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "empty.file.list.exception")
public class EmptyFileListException extends ManagedException {

    /**
     * Instantiates a new Object not found exception.
     *
     * @param message the message
     */
    public EmptyFileListException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Object not found exception.
     *
     * @param throwable the throwable
     */
    public EmptyFileListException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Object not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EmptyFileListException(String message, Throwable cause) {
        super(message, cause);
    }
}
