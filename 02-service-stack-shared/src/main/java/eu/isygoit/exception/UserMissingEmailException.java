package eu.isygoit.exception;
/**
 * @author isygoit
 */


import eu.isygoit.annotation.MsgLocale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The type User missing email exception.
 */
@ResponseStatus(HttpStatus.CONFLICT)
@MsgLocale("user.missing.email.exception")
public class UserMissingEmailException extends ManagedException {

    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new User missing email exception.
     *
     * @param message the message
     */
    public UserMissingEmailException(final String message) {
        super(message);
    }

}
