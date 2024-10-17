package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Calendar already exists exception.
 */
@MsgLocale("calendar.already.exists.exception")
public class CalendarAlreadyExistsException extends ManagedException {

    /**
     * Instantiates a new Calendar already exists exception.
     *
     * @param message the message
     */
    public CalendarAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Calendar already exists exception.
     *
     * @param throwable the throwable
     */
    public CalendarAlreadyExistsException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Calendar already exists exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public CalendarAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
