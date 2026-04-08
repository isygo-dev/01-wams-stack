package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Backup command exception.
 */
@MsgLocale(value = "timeline.event.dispatch.exception")
public class TimelineEventDispatchException extends ManagedException {

    /**
     * Instantiates a new Backup command exception.
     *
     * @param message the message
     */
    public TimelineEventDispatchException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Backup command exception.
     *
     * @param cause the cause
     */
    public TimelineEventDispatchException(Throwable cause) {
        super(cause);
    }

    public TimelineEventDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
