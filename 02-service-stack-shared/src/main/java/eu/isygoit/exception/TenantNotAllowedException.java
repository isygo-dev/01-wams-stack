package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import org.apache.hc.core5.http.HttpStatus;


/**
 * The type Operation not allowed exception.
 */
@MsgLocale("tenant.not.allowed.exception")
public class TenantNotAllowedException extends ManagedException {

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     */
    public TenantNotAllowedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param throwable the throwable
     */
    public TenantNotAllowedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Operation not allowed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TenantNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getHttpStatus() {
        return HttpStatus.SC_UNAUTHORIZED;
    }
}
