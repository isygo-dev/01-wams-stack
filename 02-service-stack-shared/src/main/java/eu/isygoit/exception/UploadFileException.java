package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Upload file exception.
 */
@MsgLocale("upload.file.exception")
public class UploadFileException extends ManagedException {

    /**
     * Instantiates a new Upload file exception.
     *
     * @param message the message
     */
    public UploadFileException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Upload file exception.
     *
     * @param throwable the throwable
     */
    public UploadFileException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Upload file exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
