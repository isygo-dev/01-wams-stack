package eu.isygoit.exception.handler;

import java.util.Map;

/**
 * The interface Exception handler.
 */
public interface IExceptionHandler {

    /**
     * Handle error string.
     *
     * @param e the e
     * @return the string
     */
    String handleError(Throwable e);

    /**
     * Handle message string.
     *
     * @param message the message
     * @return the string
     */
    String handleMessage(String message);

    /**
     * Gets stack trace.
     *
     * @param throwable the throwable
     * @return the stack trace
     */
    String getStackTrace(Throwable throwable);

    /**
     * Process unmanaged exception.
     *
     * @param message the message
     */
    void processUnmanagedException(String message);

    /**
     * Gets entity map.
     *
     * @return the entity map
     */
    Map<String, Class<?>> getEntityClasses();
}
