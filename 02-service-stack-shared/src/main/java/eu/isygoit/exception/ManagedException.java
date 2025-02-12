package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * The type Managed exception.
 */
@Slf4j
public abstract class ManagedException extends RuntimeException {

    /**
     * Instantiates a new Managed exception.
     */
    public ManagedException() {
        super();
    }

    /**
     * Instantiates a new Managed exception.
     *
     * @param message the message
     */
    public ManagedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Managed exception.
     *
     * @param cause the cause
     */
    public ManagedException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Managed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public ManagedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Managed exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public ManagedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Gets msg locale.
     *
     * @return the msg locale
     */
    public String getMsgLocale() {
        String value = null;
        MsgLocale msgLocale = this.getClass().getAnnotation(MsgLocale.class);
        if (msgLocale != null && StringUtils.hasText(msgLocale.value())) {
            value = msgLocale.value();
        } else {
            log.error("<Error>: msgLocale annotation not defined for class type {}", this.getClass().getSimpleName());
        }
        if (!StringUtils.hasText(value)) {
            return new StringBuilder("Message key not defined for managed exception:")
                    .append(this.getClass().getSimpleName())
                    .toString();
        }
        return value;
    }
}
