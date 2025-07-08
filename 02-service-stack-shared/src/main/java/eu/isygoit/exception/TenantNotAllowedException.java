package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Operation not allowed exception.
 */
@MsgLocale(value = "tenant.not.allowed.exception")
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
}
