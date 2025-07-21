package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Bad argument exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "bad.argument.exception")
public class BadArgumentException extends ManagedException {

    /**
     * Instantiates a new Bad argument exception.
     *
     * @param message the message
     */
    public BadArgumentException(String message) {
        super(message);
    }
}
