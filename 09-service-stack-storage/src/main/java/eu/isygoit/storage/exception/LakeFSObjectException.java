package eu.isygoit.storage.exception;

import eu.isygoit.annotation.MsgLocale;
import eu.isygoit.exception.ManagedException;


/**
 * The type Min io object exception.
 */
@MsgLocale("lakefs.get.object.exception")
public class LakeFSObjectException extends ManagedException {

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     */
    public LakeFSObjectException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param throwable the throwable
     */
    public LakeFSObjectException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Min io object exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public LakeFSObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
