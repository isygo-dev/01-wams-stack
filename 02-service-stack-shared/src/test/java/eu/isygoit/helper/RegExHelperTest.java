package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The type Reg ex helper test.
 */
@DisplayName("RegExHelper Tests")
class RegExHelperTest {

    /**
     * The type String validation tests.
     */
    @Nested
    @DisplayName("String Validation Tests")
    class StringValidationTests {

        /**
         * Validate string.
         */
        @Test
        @DisplayName("Should validate string against regex pattern")
        void validateString() {
            assertTrue(RegExHelper.validateString("test123", "\\w+"));
        }

        /**
         * Validate string fail.
         */
        @Test
        @DisplayName("Should fail validation for invalid string")
        void validateStringFail() {
            assertFalse(RegExHelper.validateString("test@123", "^[a-zA-Z]+$"));
        }
    }

    /**
     * The type Uuid validation tests.
     */
    @Nested
    @DisplayName("UUID Validation Tests")
    class UUIDValidationTests {

        /**
         * Validate valid uuid.
         */
        @Test
        @DisplayName("Should validate correct UUID format")
        void validateValidUUID() {
            String validUUID = "123e4567-e89b-12d3-a456-426614174000";
            assertTrue(RegExHelper.validateUUID(validUUID));
        }

        /**
         * Validate invalid uuid.
         */
        @Test
        @DisplayName("Should reject invalid UUID format")
        void validateInvalidUUID() {
            String invalidUUID = "123-456-789";
            assertFalse(RegExHelper.validateUUID(invalidUUID));
        }
    }

    /**
     * The type Credit card validation tests.
     */
    @Nested
    @DisplayName("Credit Card Validation Tests")
    class CreditCardValidationTests {

        /**
         * Validate valid credit card.
         */
        @Test
        @DisplayName("Should validate valid credit card number")
        void validateValidCreditCard() {
            // Valid VISA test number
            assertTrue(RegExHelper.validateCreditCard("4532015112830366"));
        }

        /**
         * Validate credit card with spaces.
         */
        @Test
        @DisplayName("Should validate credit card with spaces and dashes")
        void validateCreditCardWithSpaces() {
            assertTrue(RegExHelper.validateCreditCard("4532-0151-1283-0366"));
        }

        /**
         * Validate invalid credit card.
         */
        @Test
        @DisplayName("Should reject invalid credit card number")
        void validateInvalidCreditCard() {
            assertFalse(RegExHelper.validateCreditCard("1234567890"));
        }

        /**
         * Validate null credit card.
         */
        @Test
        @DisplayName("Should reject null credit card number")
        void validateNullCreditCard() {
            assertFalse(RegExHelper.validateCreditCard(null));
        }

        /**
         * Validate empty credit card.
         */
        @Test
        @DisplayName("Should reject empty credit card number")
        void validateEmptyCreditCard() {
            assertFalse(RegExHelper.validateCreditCard(""));
        }
    }

    /**
     * The type Email validation tests.
     */
    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        /**
         * Validate valid email.
         */
        @Test
        @DisplayName("Should validate correct email format")
        void validateValidEmail() {
            assertTrue(RegExHelper.validateEmail("test@example.com"));
            assertTrue(RegExHelper.validateEmail("user.name+tag@example.co.uk"));
            assertTrue(RegExHelper.validateEmail("user_name@domain.com"));
        }

        /**
         * Validate invalid email.
         */
        @Test
        @DisplayName("Should reject invalid email format")
        void validateInvalidEmail() {
            assertFalse(RegExHelper.validateEmail("invalid.email@"));
            assertFalse(RegExHelper.validateEmail("@domain.com"));
            assertFalse(RegExHelper.validateEmail("email@.com"));
        }
    }

    /**
     * The type Phone number validation tests.
     */
    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneNumberValidationTests {

        /**
         * Validate valid phone numbers.
         */
        @Test
        @DisplayName("Should validate various phone number formats")
        void validateValidPhoneNumbers() {
            assertTrue(RegExHelper.validatePhoneNumber("+1-555-555-5555"));
            assertTrue(RegExHelper.validatePhoneNumber("(555) 555-5555"));
            assertTrue(RegExHelper.validatePhoneNumber("5555555555"));
            assertTrue(RegExHelper.validatePhoneNumber("+44 20 7123 4567"));
        }

        /**
         * Validate invalid phone number.
         */
        @Test
        @DisplayName("Should reject invalid phone number format")
        void validateInvalidPhoneNumber() {
            assertFalse(RegExHelper.validatePhoneNumber("abc-def-ghij"));
            assertFalse(RegExHelper.validatePhoneNumber("12"));
            assertFalse(RegExHelper.validatePhoneNumber("++1234567890"));
        }
    }

    /**
     * The type Isin validation tests.
     */
    @Nested
    @DisplayName("ISIN Validation Tests")
    class IsinValidationTests {

        /**
         * Validate valid isin.
         */
        @Test
        @DisplayName("Should validate correct ISIN format and checksum")
        void validateValidIsin() {
            // Valid ISIN examples
            assertTrue(RegExHelper.validateIsin("US0378331005")); // Apple Inc.
            assertTrue(RegExHelper.validateIsin("DE0007164600")); // SAP SE
            assertTrue(RegExHelper.validateIsin("GB0002374006")); // Diageo
        }

        /**
         * Validate isin format.
         */
        @Test
        @DisplayName("Should validate ISIN format separately")
        void validateIsinFormat() {
            assertTrue(RegExHelper.validateIsinFormat("US0378331005"));
            assertTrue(RegExHelper.validateIsinFormat("DE0007164600"));
            assertFalse(RegExHelper.validateIsinFormat("invalid"));
            assertFalse(RegExHelper.validateIsinFormat("US123")); // Too short
            assertFalse(RegExHelper.validateIsinFormat("123456789012")); // No country code
        }

        /**
         * Validate isin checksum.
         */
        @Test
        @DisplayName("Should validate ISIN checksum separately")
        void validateIsinChecksum() {
            assertTrue(RegExHelper.validateIsinChecksum("US0378331005"));
            assertFalse(RegExHelper.validateIsinChecksum("US0378331004")); // Invalid checksum
            assertFalse(RegExHelper.validateIsinChecksum("DE0007164601")); // Invalid checksum
        }

        /**
         * Validate invalid isin.
         */
        @Test
        @DisplayName("Should handle null and invalid length ISIN")
        void validateInvalidIsin() {
            assertFalse(RegExHelper.validateIsin(null));
            assertFalse(RegExHelper.validateIsin("US123")); // Too short
            assertFalse(RegExHelper.validateIsin("US03783310051")); // Too long
        }
    }

    /**
     * The type Ip address validation tests.
     */
    @Nested
    @DisplayName("IP Address Validation Tests")
    class IpAddressValidationTests {

        /**
         * Validate valid i pv 4.
         */
        @Test
        @DisplayName("Should validate correct IPv4 addresses")
        void validateValidIPv4() {
            assertTrue(RegExHelper.validateIPv4Address("192.168.1.1"));
            assertTrue(RegExHelper.validateIPv4Address("10.0.0.0"));
            assertTrue(RegExHelper.validateIPv4Address("172.16.254.1"));
            assertTrue(RegExHelper.validateIPv4Address("255.255.255.255"));
        }

        /**
         * Validate invalid i pv 4.
         */
        @Test
        @DisplayName("Should reject invalid IPv4 addresses")
        void validateInvalidIPv4() {
            assertFalse(RegExHelper.validateIPv4Address("256.256.256.256"));
            assertFalse(RegExHelper.validateIPv4Address("1.2.3.4.5"));
            assertFalse(RegExHelper.validateIPv4Address("192.168.001.1"));
        }

        /**
         * Validate valid i pv 6.
         */
        @Test
        @DisplayName("Should validate correct IPv6 addresses")
        void validateValidIPv6() {
            assertTrue(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
            assertTrue(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        }

        /**
         * Validate invalid i pv 6.
         */
        @Test
        @DisplayName("Should reject invalid IPv6 addresses")
        void validateInvalidIPv6() {
            assertFalse(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370"));
            assertFalse(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:733g")); // invalid hex
        }
    }

    /**
     * The type Date validation tests.
     */
    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        /**
         * Validate valid date.
         */
        @Test
        @DisplayName("Should validate correct date format")
        void validateValidDate() {
            assertTrue(RegExHelper.validateDate("2024-02-14"));
            assertTrue(RegExHelper.validateDate("2023-12-31"));
            assertTrue(RegExHelper.validateDate("2024-01-01"));
        }

        /**
         * Validate invalid date.
         */
        @Test
        @DisplayName("Should reject invalid date formats")
        void validateInvalidDate() {
            assertFalse(RegExHelper.validateDate("14-02-2024"));
            assertFalse(RegExHelper.validateDate("2024/02/14"));
            assertFalse(RegExHelper.validateDate("2024-2-14"));
            assertFalse(RegExHelper.validateDate("24-02-14"));
        }
    }
}