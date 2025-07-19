package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Bad argument exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "create.constraints.violation.exception")
public class CreateConstraintsViolationException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public CreateConstraintsViolationException(String message) {
        super(message);
    }
}
