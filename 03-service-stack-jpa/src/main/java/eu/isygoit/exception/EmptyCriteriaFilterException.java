package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;

/**
 * The type Empty criteria filter exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "empty.criteria.filter.exception")
public class EmptyCriteriaFilterException extends ManagedException {

    /**
     * Instantiates a new Empty criteria filter exception.
     *
     * @param message the message
     */
    public EmptyCriteriaFilterException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Empty criteria filter exception.
     *
     * @param cause the cause
     */
    public EmptyCriteriaFilterException(Throwable cause) {
        super(cause);
    }
}
