package eu.isygoit.openai.exception;

/**
 * The type Gemini api exception.
 */
public class GeminiApiException extends Exception {
    /**
     * Instantiates a new Gemini api exception.
     *
     * @param message the message
     */
    public GeminiApiException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Gemini api exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
