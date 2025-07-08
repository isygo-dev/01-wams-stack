package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;

/**
 * The type Backup command exception.
 */
@MsgLocale(value = "pm.demo.buckup.command.exception")
public class BackupCommandException extends ManagedException {

    /**
     * Instantiates a new Backup command exception.
     *
     * @param message the message
     */
    public BackupCommandException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Backup command exception.
     *
     * @param cause the cause
     */
    public BackupCommandException(Throwable cause) {
        super(cause);
    }
}
