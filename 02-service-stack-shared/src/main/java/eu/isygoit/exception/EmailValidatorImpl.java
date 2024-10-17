package eu.isygoit.exception;
/**
 * @author isygoit
 */


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The type Email validator.
 */
public class EmailValidatorImpl {

    /**
     * Validate boolean.
     *
     * @param input the input
     * @return the boolean
     */
    public static boolean validate(String input) {
        String emailRegex = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
        Pattern emailPat = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = emailPat.matcher(input);
        return matcher.find();
    }
}
