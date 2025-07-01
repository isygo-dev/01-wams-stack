package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Optional;

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
        var msgLocale = this.getClass().getAnnotation(MsgLocale.class);

        return Optional.ofNullable(msgLocale)
                .map(MsgLocale::value)
                .filter(StringUtils::hasText)
                .orElseGet(() -> {
                    log.error("<Error>: msgLocale annotation not defined for class type {}", this.getClass().getSimpleName());
                    return new StringBuilder("Message key not defined for managed exception: ")
                            .append(this.getClass().getSimpleName())
                            .toString();
                });
    }

    public int getHttpStatus() {
        return HttpStatus.SC_OK;
    }
}
