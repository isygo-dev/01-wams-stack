package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Msg template not found exception.
 */
@MsgLocale("message.template.not.found.exception")
public class MsgTemplateNotFoundException extends ManagedException {

    /**
     * Instantiates a new Msg template not found exception.
     *
     * @param message the message
     */
    public MsgTemplateNotFoundException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Msg template not found exception.
     *
     * @param throwable the throwable
     */
    public MsgTemplateNotFoundException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Msg template not found exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public MsgTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
