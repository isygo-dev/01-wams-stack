package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Udp server start failed exception.
 */
@MsgLocale("udp.server.start.failed")
public class UDPServerStartFailedException extends ManagedException {

    /**
     * Instantiates a new Udp server start failed exception.
     */
    public UDPServerStartFailedException() {
    }

    /**
     * Instantiates a new Udp server start failed exception.
     *
     * @param message the message
     */
    public UDPServerStartFailedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Udp server start failed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UDPServerStartFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Udp server start failed exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public UDPServerStartFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Instantiates a new Udp server start failed exception.
     *
     * @param cause the cause
     */
    public UDPServerStartFailedException(Throwable cause) {
        super(cause);
    }
}
