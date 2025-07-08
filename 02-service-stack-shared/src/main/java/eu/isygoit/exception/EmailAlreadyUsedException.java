package eu.isygoit.exception;
/**
 * @author isygoit
 */


import eu.isygoit.annotation.MsgLocale;

/**
 * The type Email already used exception.
 */
@MsgLocale(value = "email.already.used.exception")
public class EmailAlreadyUsedException extends ManagedException {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Email already used exception.
     *
     * @param message the message
     */
    public EmailAlreadyUsedException(final String message) {
        super(message);
    }

}
