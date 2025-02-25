package eu.isygoit.exception;
/**
 * @author isygoit
 */


import eu.isygoit.annotation.MsgLocale;

/**
 * The type Backend service exception.
 */
@MsgLocale("backend.service.exception")
public class BackendServiceException extends ManagedException {

    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Backend service exception.
     */
    public BackendServiceException() {
    }

    /**
     * Instantiates a new Backend service exception.
     *
     * @param message the message
     */
    public BackendServiceException(String message) {
        super(message);
    }
}
