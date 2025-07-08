package eu.isygoit.exception;
/**
 * @author isygoit
 */


import eu.isygoit.annotation.MsgLocale;

/**
 * The type To json conversion exception.
 */
@MsgLocale(value = "to.json.conversion.exception")
public class ToJsonConversionException extends ManagedException {

    /**
     * The constant serialVersionUID.
     */
    public static final long serialVersionUID = 1L;

    /**
     * Instantiates a new To json conversion exception.
     */
    public ToJsonConversionException() {
    }

    /**
     * Instantiates a new To json conversion exception.
     *
     * @param message the message
     */
    public ToJsonConversionException(String message) {
        super(message);
    }
}
