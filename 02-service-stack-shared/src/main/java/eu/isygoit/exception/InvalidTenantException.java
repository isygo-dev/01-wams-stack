package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;


/**
 * The type Operation not allowed exception.
 */
@MsgLocale(httpStatus = HttpStatus.BAD_REQUEST, value = "invalid.tenant.exception")
public class InvalidTenantException extends ManagedException {

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     */
    public InvalidTenantException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param throwable the throwable
     */
    public InvalidTenantException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public InvalidTenantException(String message, Throwable cause) {
        super(message, cause);
    }
}
