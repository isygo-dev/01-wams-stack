package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Bad argument exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "object.already.exists.exception")
public class ObjectAlreadyExistsException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public ObjectAlreadyExistsException(String message) {
        super(message);
    }
}
