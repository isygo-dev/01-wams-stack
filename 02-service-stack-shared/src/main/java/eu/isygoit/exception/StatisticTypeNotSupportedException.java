package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Statistic type not supported exception.
 */
@MsgLocale("statistic.type.not.supported.exception")
public class StatisticTypeNotSupportedException extends ManagedException {

    /**
     * Instantiates a new Statistic type not supported exception.
     *
     * @param message the message
     */
    public StatisticTypeNotSupportedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Statistic type not supported exception.
     *
     * @param throwable the throwable
     */
    public StatisticTypeNotSupportedException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Statistic type not supported exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public StatisticTypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
