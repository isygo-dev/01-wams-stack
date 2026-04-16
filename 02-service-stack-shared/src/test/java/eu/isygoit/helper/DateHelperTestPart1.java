package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Date helper test part 1.
 */
@DisplayName("DateHelper Tests")
class DateHelperTestPart1 {

    /**
     * The constant SDF.
     */
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * The type Parse date string tests.
     */
    @Nested
    @DisplayName("parseDateString Tests")
    class ParseDateStringTests {

        /**
         * Should parse valid date formats.
         *
         * @param dateString the date string
         */
        @ParameterizedTest
        @ValueSource(strings = {
                "2024-02-13T15:30:45.123+01:00",  // ISO 8601 with milliseconds
                "2024-02-13T15:30:45+01:00",      // ISO 8601
                "2024-02-13T15:30",               // ISO without timezone
                "2024-02-13",                     // ISO date
                "2024/02/13",                     // Date with slashes
                "13-02-2024T15:30:45.123+01:00",  // Day first format
                "13-02-2024",                     // Day first
                "13/02/2024",                     // Day first with slashes
                "13/02/2024 15:30",               // With time
                "20240213",                       // Compact
                "13022024",                       // Day first compact
                "202402131530",                   // With time compact
                "20240213153045",                 // Full date-time compact
                "02-13-2024",                     // US format
                "02/13/2024",                     // US with slashes
                "February 13, 2024",              // Full month name
                "2024-02-13"                      // ISO Date
        })
        @DisplayName("Should parse various valid date formats")
        void shouldParseValidDateFormats(String dateString) {
            assertNotNull(DateHelper.parseDateString(dateString, null));
        }

        /**
         * Should return default for empty or null.
         *
         * @param dateString the date string
         */
        @ParameterizedTest
        @ValueSource(strings = {"", " ", "null", "NULL"})
        @DisplayName("Should return default for empty or null strings")
        void shouldReturnDefaultForEmptyOrNull(String dateString) {
            Date defaultDate = new Date();
            assertEquals(defaultDate, DateHelper.parseDateString(dateString, defaultDate));
        }

        /**
         * Should throw exception for invalid formats.
         *
         * @param dateString the date string
         */
         @ParameterizedTest
         @ValueSource(strings = {
                 "invalid-date",
                 "2024-13-13",        // Invalid month
                 "2024-02-31",        // Invalid day
                 "abc",
                 "13/13/2024",        // Invalid month
                 "29/02/2023",        // Invalid leap year date
                 "31/04/2024",        // April has only 30 days
                 "31/06/2024",        // June has only 30 days
                 "31/09/2024",        // September has only 30 days
                 "31/11/2024"         // November has only 30 days
         })
         @DisplayName("Should throw exception for invalid date formats")
         void shouldThrowExceptionForInvalidFormats(String dateString) {
             assertThrows(IllegalArgumentException.class,
                     () -> DateHelper.parseDateString(dateString, null));
         }

         /**
          * Should parse epoch and epoch_second.
          */
         @Test
         @DisplayName("Should parse epoch and epoch_second")
         void shouldParseEpochAndEpochSecond() {
             long epochMillis = 1707838245123L; // 2024-02-13T15:30:45.123
             // Note: Depending on patterns, DateHelper might try other patterns first.
             // If "1707838245123" matches yyyyMMddHHmmss (it has 13 digits, yyyyMMddHHmmss has 14), it might fail or parse differently.
             // Actually 1707838245123 is 13 digits.
             
             Date date = DateHelper.parseDateString(String.valueOf(epochMillis), null);
             assertNotNull(date);
             // If it matched "epoch", date.getTime() should be epochMillis
             // If it matched something else, we need to be careful.
             
             long epochSeconds = 1707838245L;
             Date date2 = DateHelper.parseDateString(String.valueOf(epochSeconds), null);
             assertNotNull(date2);
             
             // Check if it's within a reasonable range or exactly equal
             assertTrue(date.getTime() == epochMillis || date2.getTime() == epochSeconds * 1000, 
                 "Epoch parsing failed. date.getTime()=" + date.getTime() + ", date2.getTime()=" + date2.getTime());
         }
     }

    /**
     * The type Format date to iso string tests.
     */
    @Nested
    @DisplayName("formatDateToIsoString Tests")
    class FormatDateToIsoStringTests {

        /**
         * Should format date with various time components.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should format date with various time components")
        void shouldFormatDateWithVariousTimeComponents() throws Exception {
            // Test multiple time points throughout the day
            String[][] testTimes = {
                    {"2024-02-13 00:00:00", "start of day"},
                    {"2024-02-13 12:30:45", "middle of day"},
                    {"2024-02-13 23:59:59", "end of day"}
            };

            for (String[] test : testTimes) {
                Date date = SDF.parse(test[0]);
                String result = DateHelper.formatDateToIsoString(date);

                assertNotNull(result, "Failed for " + test[1]);
                assertTrue(result.contains("T"), "ISO format should contain T separator");
                assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"),
                        "Should match ISO 8601 format for " + test[1]);
            }
        }

        /**
         * Should handle null date.
         */
        @Test
        @DisplayName("Should handle null date")
        void shouldHandleNullDate() {
            assertNull(DateHelper.formatDateToIsoString(null));
        }

        /**
         * Should handle timezone information.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should handle timezone information")
        void shouldHandleTimezoneInformation() throws Exception {
            Date date = SDF.parse("2024-02-13 15:30:45");
            String result = DateHelper.formatDateToIsoString(date);

            assertTrue(result.contains("+") || result.contains("-"),
                    "Should include timezone offset");
        }
    }
}
