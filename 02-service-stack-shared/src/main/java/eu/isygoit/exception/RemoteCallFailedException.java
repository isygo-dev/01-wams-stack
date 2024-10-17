package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Remote call failed exception.
 */
@MsgLocale("remote.call.failed.exception")
public class RemoteCallFailedException extends ManagedException {

    /**
     * Instantiates a new Remote call failed exception.
     *
     * @param message the message
     */
    public RemoteCallFailedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Remote call failed exception.
     *
     * @param throwable the throwable
     */
    public RemoteCallFailedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Remote call failed exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public RemoteCallFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
