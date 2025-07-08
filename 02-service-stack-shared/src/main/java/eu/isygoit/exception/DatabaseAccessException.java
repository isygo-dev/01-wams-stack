package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Database access exception.
 */
@MsgLocale(value = "database.access.exception")
public class DatabaseAccessException extends ManagedException {

    /**
     * Instantiates a new Database access exception.
     *
     * @param message the message
     */
    public DatabaseAccessException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Database access exception.
     *
     * @param throwable the throwable
     */
    public DatabaseAccessException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Database access exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public DatabaseAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
