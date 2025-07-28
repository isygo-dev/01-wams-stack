package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Min io s 3 bucket exception.
 */
@MsgLocale("minio.s3.bucket.exception")
public class MinIoS3BucketException extends ManagedException {

    /**
     * Instantiates a new Min io s 3 bucket exception.
     *
     * @param message the message
     */
    public MinIoS3BucketException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Min io s 3 bucket exception.
     *
     * @param throwable the throwable
     */
    public MinIoS3BucketException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Instantiates a new Min io s 3 bucket exception.
     *
     * @param message the message
     * @param cause   the cause
     */
    public MinIoS3BucketException(String message, Throwable cause) {
        super(message, cause);
    }
}
