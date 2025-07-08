package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;

/**
 * The type Wrong criteria filter exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "wrong.criteria.filter.exception")
public class WrongCriteriaFilterException extends ManagedException {

    /**
     * Instantiates a new Wrong criteria filter exception.
     *
     * @param message the message
     */
    public WrongCriteriaFilterException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Wrong criteria filter exception.
     *
     * @param cause the cause
     */
    public WrongCriteriaFilterException(Throwable cause) {
        super(cause);
    }
}
