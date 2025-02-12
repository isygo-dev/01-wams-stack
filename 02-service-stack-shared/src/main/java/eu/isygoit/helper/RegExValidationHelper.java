package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * The type RegExValidationHelper provides utility methods for validating strings
 * against regular expressions and other common validation checks, including UUID, Credit Card, ISIN, Email, Phone Numbers, and more.
 */
public interface RegExValidationHelper {

    Logger logger = LoggerFactory.getLogger(SecurityHelper.class);

    /**
     * Validate a string against a given regular expression.
     * This method checks if the provided string matches the given regex pattern.
     *
     * @param string the string to validate
     * @param regExp the regular expression to validate against
     * @return {@code true} if the string matches the regular expression, otherwise {@code false}
     */
    public static boolean validateString(String string, String regExp) {
        // Compile the regular expression and check if it matches the string
        Pattern pattern = Pattern.compile(regExp);
        boolean validateSyntax = pattern.matcher(string).find();

        // Log an error if the string does not match the pattern
        if (!validateSyntax) {
            logger.error("<Error>: Validation failed for string: '{}', using RegExp: '{}'", string, pattern);
            return false;
        }

        // Return true if the string is valid
        logger.info("Validation passed for string: '{}', using RegExp: '{}'", string, pattern);
        return true;
    }

    /**
     * Validate UUID format.
     * <p>
     * This method checks if the given string is a valid UUID.
     * UUIDs are typically 36 characters with 32 hexadecimal digits and hyphens separating certain sections.
     *
     * @param uuid the UUID string to validate
     * @return {@code true} if the UUID format is valid, otherwise {@code false}
     */
    public static boolean validateUUID(String uuid) {
        var uuidPattern = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        boolean isValid = validateString(uuid, uuidPattern);
        if (isValid) {
            logger.info("UUID is valid: '{}'", uuid);
        } else {
            logger.error("<Error>: Invalid UUID format: '{}'", uuid);
        }
        return isValid;
    }

    /**
     * Validate Credit Card number using Luhn's algorithm.
     * <p>
     * This method validates the credit card number format and performs a checksum validation based on Luhn's algorithm.
     *
     * @param cardNumber the credit card number to validate
     * @return {@code true} if the card number is valid, otherwise {@code false}
     */
    public static boolean validateCreditCard(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            logger.error("<Error>: Credit card number is empty.");
            return false;
        }

        // Remove spaces and dashes from the card number
        cardNumber = cardNumber.replaceAll("[\\s-]", "");

        // Validate numeric format
        if (!cardNumber.matches("\\d+")) {
            logger.error("<Error>: Invalid credit card number format (must be numeric): '{}'", cardNumber);
            return false;
        }

        // Perform Luhn's algorithm for checksum validation
        int sum = 0;
        boolean shouldDouble = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = cardNumber.charAt(i) - '0';
            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) digit -= 9;  // Adjust if the result is greater than 9
            }
            sum += digit;
            shouldDouble = !shouldDouble;
        }

        // If the sum modulo 10 equals 0, the card number is valid
        boolean isValid = sum % 10 == 0;
        if (isValid) {
            logger.info("Credit card number is valid: '{}'", cardNumber);
        } else {
            logger.error("<Error>: Invalid credit card number: '{}'", cardNumber);
        }
        return isValid;
    }

    /**
     * Validate email format.
     * <p>
     * This method checks if the given email address matches a basic email format.
     *
     * @param email the email to validate
     * @return {@code true} if the email format is valid, otherwise {@code false}
     */
    public static boolean validateEmail(String email) {
        var emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$"; // Simple regex for email validation
        boolean isValid = validateString(email, emailPattern);
        if (isValid) {
            logger.info("Email is valid: '{}'", email);
        } else {
            logger.error("<Error>: Invalid email format: '{}'", email);
        }
        return isValid;
    }

    /**
     * Validate phone number format.
     * <p>
     * This method validates phone numbers that may include a country code, area code, and local number.
     *
     * @param phoneNumber the phone number to validate
     * @return {@code true} if the phone number format is valid, otherwise {@code false}
     */
    public static boolean validatePhoneNumber(String phoneNumber) {
        var phonePattern = "^(\\+\\d{1,3}[- ]?)?\\(?\\d{1,4}\\)?[- ]?\\d{1,4}[- ]?\\d{1,4}$";
        boolean isValid = validateString(phoneNumber, phonePattern);
        if (isValid) {
            logger.info("Phone number is valid: '{}'", phoneNumber);
        } else {
            logger.error("<Error>: Invalid phone number format: '{}'", phoneNumber);
        }
        return isValid;
    }

    /**
     * Validate date format (yyyy-MM-dd).
     * <p>
     * This method checks if the date string is in the standard "yyyy-MM-dd" format.
     *
     * @param date the date string to validate
     * @return {@code true} if the date format is valid, otherwise {@code false}
     */
    public static boolean validateDate(String date) {
        var datePattern = "^\\d{4}-\\d{2}-\\d{2}$"; // Date format: yyyy-MM-dd
        boolean isValid = validateString(date, datePattern);
        if (isValid) {
            logger.info("Date is valid: '{}'", date);
        } else {
            logger.error("<Error>: Invalid date format: '{}'", date);
        }
        return isValid;
    }

    /**
     * Validate IPv4 address format.
     * <p>
     * This method checks if the provided string is a valid IPv4 address.
     * IPv4 addresses consist of four numbers between 0 and 255, separated by dots.
     *
     * @param ipAddress the IPv4 address to validate
     * @return {@code true} if the IPv4 address format is valid, otherwise {@code false}
     */
    public static boolean validateIPv4Address(String ipAddress) {
        var ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        boolean isValid = validateString(ipAddress, ipv4Pattern);
        if (isValid) {
            logger.info("IPv4 address is valid: '{}'", ipAddress);
        } else {
            logger.error("<Error>: Invalid IPv4 address format: '{}'", ipAddress);
        }
        return isValid;
    }

    /**
     * Validate IPv6 address format.
     * <p>
     * This method checks if the provided string is a valid IPv6 address.
     * IPv6 addresses consist of eight groups of four hexadecimal digits separated by colons.
     *
     * @param ipAddress the IPv6 address to validate
     * @return {@code true} if the IPv6 address format is valid, otherwise {@code false}
     */
    public static boolean validateIPv6Address(String ipAddress) {
        var ipv6Pattern = "([0-9a-fA-F]{1,4}:){7}([0-9a-fA-F]{1,4})";
        boolean isValid = validateString(ipAddress, ipv6Pattern);
        if (isValid) {
            logger.info("IPv6 address is valid: '{}'", ipAddress);
        } else {
            logger.error("<Error>: Invalid IPv6 address format: '{}'", ipAddress);
        }
        return isValid;
    }

    // ISIN validation methods

    /**
     * Validate ISIN format.
     * <p>
     * This method checks if the given ISIN string matches the standard ISIN format (2 letters + 10 digits).
     *
     * @param isin the ISIN to validate
     * @return {@code true} if the ISIN format is valid, otherwise {@code false}
     */
    public static boolean validateIsinFormat(String isin) {
        var isinPattern = "^[A-Z]{2}\\d{10}$"; // ISIN format: 2 letters (country code) + 10 digits
        boolean isValid = validateString(isin, isinPattern);
        if (isValid) {
            logger.info("ISIN format is valid: '{}'", isin);
        } else {
            logger.error("<Error>: Invalid ISIN format: '{}'", isin);
        }
        return isValid;
    }

    /**
     * Validate ISIN checksum.
     * <p>
     * This method checks the checksum of the ISIN using the Luhn algorithm.
     *
     * @param isin the ISIN to validate
     * @return {@code true} if the ISIN checksum is valid, otherwise {@code false}
     */
    public static boolean validateIsinChecksum(String isin) {
        if (isin == null || isin.length() != 12) {
            logger.error("<Error>: Invalid ISIN length: '{}'", isin);
            return false;
        }

        // Convert letters to numbers (A = 10, B = 11, ..., Z = 35)
        StringBuilder convertedIsin = new StringBuilder();
        for (char c : isin.toCharArray()) {
            if (Character.isLetter(c)) {
                // Convert letters to numbers
                convertedIsin.append(Character.toUpperCase(c) - 'A' + 10);
            } else {
                convertedIsin.append(c); // Append digits as they are
            }
        }

        // Reverse the string to apply Luhn's algorithm from the end
        String reversed = convertedIsin.reverse().toString();

        // Implement the Luhn algorithm for checksum validation
        int sum = 0;
        for (int i = 0; i < reversed.length(); i++) {
            int digit = Character.getNumericValue(reversed.charAt(i));

            // Double every second digit starting from the right
            if (i % 2 == 1) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9; // Subtract 9 if the result is greater than 9
                }
            }

            sum += digit;
        }

        // If the sum modulo 10 is 0, the checksum is valid
        boolean isValid = sum % 10 == 0;
        if (isValid) {
            logger.info("ISIN checksum is valid: '{}'", isin);
        } else {
            logger.error("<Error>: Invalid ISIN checksum: '{}'", isin);
        }

        return isValid;
    }

    /**
     * Validate ISIN (format and checksum).
     * <p>
     * This method validates both the ISIN format and its checksum. It returns {@code true}
     * only if both the format and the checksum are valid.
     *
     * @param isin the ISIN to validate
     * @return {@code true} if both the ISIN format and checksum are valid, otherwise {@code false}
     */
    public static boolean validateIsin(String isin) {
        boolean isValidFormat = validateIsinFormat(isin);
        boolean isValidChecksum = isValidFormat && validateIsinChecksum(isin);

        if (isValidChecksum) {
            logger.info("ISIN is valid: '{}'", isin);
        } else {
            logger.error("<Error>: ISIN is invalid: '{}'", isin);
        }

        return isValidChecksum;
    }
}