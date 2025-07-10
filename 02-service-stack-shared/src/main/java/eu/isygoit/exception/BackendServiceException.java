package eu.isygoit.exception;
/**
 * @author isygoit
 */


import eu.isygoit.annotation.MsgLocale;

/**
 * The type Backend api exception.
 */
@MsgLocale(value = "backend.api.exception")
public class BackendServiceException extends ManagedException {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Backend api exception.
     */
    public BackendServiceException() {
    }

    /**
     * Instantiates a new Backend api exception.
     *
     * @param message the message
     */
    public BackendServiceException(String message) {
        super(message);
    }
}
