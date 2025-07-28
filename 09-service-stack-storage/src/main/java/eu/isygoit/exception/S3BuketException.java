package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type S 3 buket exception.
 */
@MsgLocale("s3.bucket.exception")
public class S3BuketException extends ManagedException {

    /**
     * Instantiates a new S 3 buket exception.
     *
     * @param message the message
     */
    public S3BuketException(String message) {
        super(message);
    }

    /**
     * Instantiates a new S 3 buket exception.
     *
     * @param throwable the throwable
     */
    public S3BuketException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new S 3 buket exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public S3BuketException(String message, Throwable cause) {
        super(message, cause);
    }
}
