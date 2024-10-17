package eu.isygoit.exception;
/**
 * @author isygoit
 */


import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type Email already used exception.
 */
@ResponseStatus(HttpStatus.CONFLICT)
@MsgLocale("email.already.used.exception")
public class EmailAlreadyUsedException extends ManagedException {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Email already used exception.
     *
     * @param message the message
     */
    public EmailAlreadyUsedException(final String message) {
        super(message);
    }

}
