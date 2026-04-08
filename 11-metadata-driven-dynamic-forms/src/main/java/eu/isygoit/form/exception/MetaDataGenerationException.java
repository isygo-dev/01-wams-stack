package eu.isygoit.form.exception;

public class MetaDataGenerationException extends RuntimeException {
    public MetaDataGenerationException(String message) {
        super(message);
    }

    public MetaDataGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}