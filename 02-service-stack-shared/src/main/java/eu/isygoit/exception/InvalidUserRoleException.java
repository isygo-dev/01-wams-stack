package eu.isygoit.exception;

import eu.isygoit.annotation.MsgLocale;


/**
 * The type Invalid user role exception.
 */
@MsgLocale("invalid.user.role.exception")
public class InvalidUserRoleException extends ManagedException {

    /**
     * Instantiates a new Invalid user role exception.
     *
     * @param s the s
     */
    public InvalidUserRoleException(String s) {
    }
}
