package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Object not found exception.
 */
@MsgLocale(httpStatus = HttpStatus.NOT_MODIFIED, value = "object.not.modified")
public class ObjectNotModifiedException extends ManagedException {

    /**
     * Instantiates a new Object not found exception.
     *
     * @param message the message
     */
    public ObjectNotModifiedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Object not found exception.
     *
     * @param throwable the throwable
     */
    public ObjectNotModifiedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Object not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ObjectNotModifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
