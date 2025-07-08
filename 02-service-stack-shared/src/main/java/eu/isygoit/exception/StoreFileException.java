package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Store file exception.
 */
@MsgLocale(value = "store.file.exception")
public class StoreFileException extends ManagedException {

    /**
     * Instantiates a new Store file exception.
     *
     * @param message the message
     */
    public StoreFileException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Store file exception.
     *
     * @param throwable the throwable
     */
    public StoreFileException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Store file exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public StoreFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
