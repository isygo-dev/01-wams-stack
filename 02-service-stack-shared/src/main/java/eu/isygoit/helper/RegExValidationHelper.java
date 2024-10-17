package eu.isygoit.helper;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * The type Reg ex validation helper.
 */
@Slf4j
public class RegExValidationHelper {

    /**
     * Validate string boolean.
     *
     * @param string the string
     * @param regExp the reg exp
     * @return the boolean
     */
    public static boolean validateString(String string, String regExp) {
        Pattern pattern = Pattern.compile(regExp);
        boolean validateSyntax = pattern.matcher(string).find();

        if (!validateSyntax) {
            log.error("<Error>: Wrong format of Service File Name :{} using RegExp : {} ", string, pattern);
            return false;
        }

        return true;
    }
}
