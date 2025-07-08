package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Calendar not found exception.
 */
@MsgLocale(value = "calendar.not.found.exception")
public class CalendarNotFoundException extends ManagedException {

    /**
     * Instantiates a new Calendar not found exception.
     *
     * @param message the message
     */
    public CalendarNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Calendar not found exception.
     *
     * @param throwable the throwable
     */
    public CalendarNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Calendar not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CalendarNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
