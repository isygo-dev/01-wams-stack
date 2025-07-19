package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Bad argument exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "update.constraints.violation.exception")
public class UpdateConstraintsViolationException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public UpdateConstraintsViolationException(String message) {
        super(message);
    }
}
