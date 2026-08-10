package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Tenant context exception.
 */
@MsgLocale(value = "tenant.context.exception")
public class TenantContextException extends ManagedException {
    public TenantContextException(String message) {
        super(message);
    }

    public TenantContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
