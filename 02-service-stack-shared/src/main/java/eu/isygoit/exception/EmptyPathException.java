package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Empty path exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "empty.path.found.exception")
public class EmptyPathException extends ManagedException {

    /**
     * Instantiates a new Empty path exception.
     *
     * @param message the message
     */
    public EmptyPathException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Empty path exception.
     *
     * @param throwable the throwable
     */
    public EmptyPathException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Empty path exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public EmptyPathException(String message, Throwable cause) {
        super(message, cause);
    }
}
