package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Jpa repository not defined exception.
 */
@MsgLocale(value = "dao.not.defined.exception")
public class JpaRepositoryNotDefinedException extends ManagedException {

    /**
     * Instantiates a new Jpa repository not defined exception.
     *
     * @param message the message
     */
    public JpaRepositoryNotDefinedException(String message) {
        super(message);
    }
}
