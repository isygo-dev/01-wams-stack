package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Object instantiation exception.
 */
@MsgLocale(value = "object.instantiation.exception")
public class ObjectInstantiationException extends ManagedException {
    public ObjectInstantiationException(String message) {
        super(message);
    }

    public ObjectInstantiationException(String message, Throwable cause) {
        super(message, cause);
    }
}
