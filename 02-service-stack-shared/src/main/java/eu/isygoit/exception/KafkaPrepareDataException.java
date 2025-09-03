package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type App parameter not found exception.
 */
@MsgLocale(value = "kafka.prepare.data.exception")
public class KafkaPrepareDataException extends ManagedException {

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     */
    public KafkaPrepareDataException(String message) {
        super(message);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param throwable the throwable
     */
    public KafkaPrepareDataException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new App parameter not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public KafkaPrepareDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
