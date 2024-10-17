package eu.isygoit.exception;


import eu.isygoit.annotation.MsgLocale;


/**
 * The type Param not found exception.
 */
@MsgLocale("param.not.found.exception")
public class ParamNotFoundException extends ManagedException {

    /**
     * Instantiates a new Param not found exception.
     *
     * @param s the s
     */
    public ParamNotFoundException(String s) {
        super(s);
    }
}
