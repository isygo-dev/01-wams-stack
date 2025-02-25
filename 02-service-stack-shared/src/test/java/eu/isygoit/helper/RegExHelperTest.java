package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("RegExHelper Tests")
class RegExHelperTest {

    @Nested
    @DisplayName("String Validation Tests")
    class StringValidationTests {

        @Test
        @DisplayName("Should validate string against regex pattern")
        void validateString() {
            assertTrue(RegExHelper.validateString("test123", "\\w+"));
        }

        @Test
        @DisplayName("Should fail validation for invalid string")
        void validateStringFail() {
            assertFalse(RegExHelper.validateString("test@123", "^[a-zA-Z]+$"));
        }
    }

    @Nested
    @DisplayName("UUID Validation Tests")
    class UUIDValidationTests {

        @Test
        @DisplayName("Should validate correct UUID format")
        void validateValidUUID() {
            String validUUID = "123e4567-e89b-12d3-a456-426614174000";
            assertTrue(RegExHelper.validateUUID(validUUID));
        }

        @Test
        @DisplayName("Should reject invalid UUID format")
        void validateInvalidUUID() {
            String invalidUUID = "123-456-789";
            assertFalse(RegExHelper.validateUUID(invalidUUID));
        }
    }

    @Nested
    @DisplayName("Credit Card Validation Tests")
    class CreditCardValidationTests {

        @Test
        @DisplayName("Should validate valid credit card number")
        void validateValidCreditCard() {
            // Valid VISA test number
            assertTrue(RegExHelper.validateCreditCard("4532015112830366"));
        }

        @Test
        @DisplayName("Should validate credit card with spaces and dashes")
        void validateCreditCardWithSpaces() {
            assertTrue(RegExHelper.validateCreditCard("4532-0151-1283-0366"));
        }

        @Test
        @DisplayName("Should reject invalid credit card number")
        void validateInvalidCreditCard() {
            assertFalse(RegExHelper.validateCreditCard("1234567890"));
        }

        @Test
        @DisplayName("Should reject null credit card number")
        void validateNullCreditCard() {
            assertFalse(RegExHelper.validateCreditCard(null));
        }

        @Test
        @DisplayName("Should reject empty credit card number")
        void validateEmptyCreditCard() {
            assertFalse(RegExHelper.validateCreditCard(""));
        }
    }

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should validate correct email format")
        void validateValidEmail() {
            assertTrue(RegExHelper.validateEmail("test@example.com"));
            assertTrue(RegExHelper.validateEmail("user.name+tag@example.co.uk"));
            assertTrue(RegExHelper.validateEmail("user_name@domain.com"));
        }

        @Test
        @DisplayName("Should reject invalid email format")
        void validateInvalidEmail() {
            assertFalse(RegExHelper.validateEmail("invalid.email@"));
            assertFalse(RegExHelper.validateEmail("@domain.com"));
            assertFalse(RegExHelper.validateEmail("email@.com"));
        }
    }

    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneNumberValidationTests {

        @Test
        @DisplayName("Should validate various phone number formats")
        void validateValidPhoneNumbers() {
            assertTrue(RegExHelper.validatePhoneNumber("+1-555-555-5555"));
            assertTrue(RegExHelper.validatePhoneNumber("(555) 555-5555"));
            assertTrue(RegExHelper.validatePhoneNumber("5555555555"));
            assertTrue(RegExHelper.validatePhoneNumber("+44 20 7123 4567"));
        }

        @Test
        @DisplayName("Should reject invalid phone number format")
        void validateInvalidPhoneNumber() {
            assertFalse(RegExHelper.validatePhoneNumber("abc-def-ghij"));
            assertFalse(RegExHelper.validatePhoneNumber("12"));
            assertFalse(RegExHelper.validatePhoneNumber("++1234567890"));
        }
    }

    @Nested
    @DisplayName("ISIN Validation Tests")
    class IsinValidationTests {

        @Test
        @DisplayName("Should validate correct ISIN format and checksum")
        void validateValidIsin() {
            // Valid ISIN examples
            assertTrue(RegExHelper.validateIsin("US0378331005")); // Apple Inc.
            assertTrue(RegExHelper.validateIsin("DE0007164600")); // SAP SE
            assertTrue(RegExHelper.validateIsin("GB0002374006")); // Diageo
        }

        @Test
        @DisplayName("Should validate ISIN format separately")
        void validateIsinFormat() {
            assertTrue(RegExHelper.validateIsinFormat("US0378331005"));
            assertTrue(RegExHelper.validateIsinFormat("DE0007164600"));
            assertFalse(RegExHelper.validateIsinFormat("invalid"));
            assertFalse(RegExHelper.validateIsinFormat("US123")); // Too short
            assertFalse(RegExHelper.validateIsinFormat("123456789012")); // No country code
        }

        @Test
        @DisplayName("Should validate ISIN checksum separately")
        void validateIsinChecksum() {
            assertTrue(RegExHelper.validateIsinChecksum("US0378331005"));
            assertFalse(RegExHelper.validateIsinChecksum("US0378331004")); // Invalid checksum
            assertFalse(RegExHelper.validateIsinChecksum("DE0007164601")); // Invalid checksum
        }

        @Test
        @DisplayName("Should handle null and invalid length ISIN")
        void validateInvalidIsin() {
            assertFalse(RegExHelper.validateIsin(null));
            assertFalse(RegExHelper.validateIsin("US123")); // Too short
            assertFalse(RegExHelper.validateIsin("US03783310051")); // Too long
        }
    }

    @Nested
    @DisplayName("IP Address Validation Tests")
    class IpAddressValidationTests {

        @Test
        @DisplayName("Should validate correct IPv4 addresses")
        void validateValidIPv4() {
            assertTrue(RegExHelper.validateIPv4Address("192.168.1.1"));
            assertTrue(RegExHelper.validateIPv4Address("10.0.0.0"));
            assertTrue(RegExHelper.validateIPv4Address("172.16.254.1"));
            assertTrue(RegExHelper.validateIPv4Address("255.255.255.255"));
        }

        @Test
        @DisplayName("Should reject invalid IPv4 addresses")
        void validateInvalidIPv4() {
            assertFalse(RegExHelper.validateIPv4Address("256.256.256.256"));
            assertFalse(RegExHelper.validateIPv4Address("1.2.3.4.5"));
            assertFalse(RegExHelper.validateIPv4Address("192.168.001.1"));
        }

        @Test
        @DisplayName("Should validate correct IPv6 addresses")
        void validateValidIPv6() {
            assertTrue(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
            assertTrue(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334"));
        }

        @Test
        @DisplayName("Should reject invalid IPv6 addresses")
        void validateInvalidIPv6() {
            assertFalse(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370"));
            assertFalse(RegExHelper.validateIPv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:733g")); // invalid hex
        }
    }

    @Nested
    @DisplayName("Date Validation Tests")
    class DateValidationTests {

        @Test
        @DisplayName("Should validate correct date format")
        void validateValidDate() {
            assertTrue(RegExHelper.validateDate("2024-02-14"));
            assertTrue(RegExHelper.validateDate("2023-12-31"));
            assertTrue(RegExHelper.validateDate("2024-01-01"));
        }

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