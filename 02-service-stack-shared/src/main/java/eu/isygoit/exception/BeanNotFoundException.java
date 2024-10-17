package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Bean not found exception.
 */
@MsgLocale("bean.not.found.exception")
public class BeanNotFoundException extends ManagedException {

    /**
     * Instantiates a new Bean not found exception.
     *
     * @param message the message
     */
    public BeanNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Bean not found exception.
     *
     * @param throwable the throwable
     */
    public BeanNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Bean not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public BeanNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
