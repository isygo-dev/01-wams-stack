package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Tcp server start failed exception.
 */
@MsgLocale("tcp.server.start.failed")
public class TCPServerStartFailedException extends ManagedException {

    /**
     * Instantiates a new Tcp server start failed exception.
     */
    public TCPServerStartFailedException() {
    }

    /**
     * Instantiates a new Tcp server start failed exception.
     *
     * @param message the message
     */
    public TCPServerStartFailedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Tcp server start failed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public TCPServerStartFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Tcp server start failed exception.
     *
     * @param message            the message
     * @param cause              the cause
     * @param enableSuppression  the enable suppression
     * @param writableStackTrace the writable stack trace
     */
    public TCPServerStartFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    /**
     * Instantiates a new Tcp server start failed exception.
     *
     * @param cause the cause
     */
    public TCPServerStartFailedException(Throwable cause) {
        super(cause);
    }
}
