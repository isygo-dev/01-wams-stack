package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Encryption exception.
 */
@MsgLocale(value = "encryption.exception")
public class EncryptionException extends ManagedException {
    public EncryptionException(String message) {
        super(message);
    }

    public EncryptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionException(Throwable cause) {
        super(cause);
    }
}
