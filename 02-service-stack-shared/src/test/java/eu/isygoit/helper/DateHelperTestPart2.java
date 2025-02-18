package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The type Date helper test part 2.
 */
class DateHelperTestPart2 {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * The type Occurred in last x hours tests.
     */
    @Nested
    @DisplayName("occurredInLastXHours Tests")
    class OccurredInLastXHoursTests {

        /**
         * Should handle various time ranges.
         */
        @Test
        @DisplayName("Should handle various time ranges")
        void shouldHandleVariousTimeRanges() {
            Date now = new Date();

            // Test multiple hour ranges
            int[] hourRanges = {1, 6, 12, 24, 48, 168}; // 1 hour to 1 week
            for (int hours : hourRanges) {
                assertTrue(DateHelper.occurredInLastXHours(now, hours),
                        "Current time should be within last " + hours + " hours");

                Date pastDate = Date.from(Instant.now().minus(Duration.ofHours(hours + 1)));
                assertFalse(DateHelper.occurredInLastXHours(pastDate, hours),
                        "Date " + (hours + 1) + " hours ago should not be within last " + hours + " hours");
            }
        }

        /**
         * Should handle edge cases.
         */
        @Test
        @DisplayName("Should handle edge cases")
        void shouldHandleEdgeCases() {
            Date exactBoundary = Date.from(Instant.now().minus(Duration.ofHours(24)));
            assertFalse(DateHelper.occurredInLastXHours(exactBoundary, 24),
                    "Exact boundary should not be included");

            Date nearBoundary = Date.from(Instant.now().minus(Duration.ofHours(23))
                    .minus(Duration.ofMinutes(59)));
            assertTrue(DateHelper.occurredInLastXHours(nearBoundary, 24),
                    "Just within boundary should be included");
        }

        /**
         * Should handle future dates.
         */
        @Test
        @DisplayName("Should handle future dates")
        void shouldHandleFutureDates() {
            Date futureDate = Date.from(Instant.now().plus(Duration.ofHours(1)));
            assertTrue(DateHelper.occurredInLastXHours(futureDate, 24),
                    "Future dates should be considered within range");
        }
    }

    /**
     * The type Are dates on same day tests.
     */
    @Nested
    @DisplayName("areDatesOnSameDay Tests")
    class AreDatesOnSameDayTests {

        /**
         * Should compare dates correctly.
         *
         * @param date1Str the date 1 str
         * @param date2Str the date 2 str
         * @param expected the expected
         * @throws Exception the exception
         */
        @ParameterizedTest
        @CsvSource({
                "2024-02-13 00:00:00, 2024-02-13 23:59:59, true",
                "2024-02-13 00:00:00, 2024-02-14 00:00:00, false",
                "2024-02-13 12:00:00, 2024-02-13 15:30:00, true",
                "2024-12-31 23:59:59, 2025-01-01 00:00:00, false"
        })
        @DisplayName("Should compare dates correctly")
        void shouldCompareDatesCorrectly(String date1Str, String date2Str, boolean expected)
                throws Exception {
            Date date1 = SDF.parse(date1Str);
            Date date2 = SDF.parse(date2Str);

            assertEquals(expected, DateHelper.areDatesOnSameDay(date1, date2));
        }

        /**
         * Should handle special calendar cases.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should handle special calendar cases")
        void shouldHandleSpecialCalendarCases() throws Exception {
            // Test leap year boundary
            Date leapYearEnd = SDF.parse("2024-02-29 23:59:59");
            Date marchFirst = SDF.parse("2024-03-01 00:00:00");
            assertFalse(DateHelper.areDatesOnSameDay(leapYearEnd, marchFirst));

            // Test daylight saving time transition
            Date beforeDST = SDF.parse("2024-03-10 01:59:59");
            Date afterDST = SDF.parse("2024-03-10 03:00:00");
            assertTrue(DateHelper.areDatesOnSameDay(beforeDST, afterDST));
        }
    }

    /**
     * The type Date addition tests.
     */
    @Nested
    @DisplayName("addDaysToDate and addMonthsToDate Tests")
    class DateAdditionTests {

        /**
         * Should add days correctly.
         *
         * @param startDateStr    the start date str
         * @param days            the days
         * @param expectedDateStr the expected date str
         * @throws Exception the exception
         */
        @ParameterizedTest
        @CsvSource({
                "2024-02-13 12:00:00, 1, 2024-02-14 12:00:00",
                "2024-02-13 12:00:00, -1, 2024-02-12 12:00:00",
                "2024-02-28 12:00:00, 1, 2024-02-29 12:00:00",
                "2024-12-31 12:00:00, 1, 2025-01-01 12:00:00"
        })
        @DisplayName("Should add days correctly")
        void shouldAddDaysCorrectly(String startDateStr, int days, String expectedDateStr)
                throws Exception {
            Date startDate = SDF.parse(startDateStr);
            Date expectedDate = SDF.parse(expectedDateStr);

            Date result = DateHelper.addDaysToDate(startDate, days);
            assertTrue(DateHelper.areDatesOnSameDay(expectedDate, result));
        }

        /**
         * Should add months correctly.
         *
         * @param startDateStr    the start date str
         * @param months          the months
         * @param expectedDateStr the expected date str
         * @throws Exception the exception
         */
        @ParameterizedTest
        @CsvSource({
                "2024-02-13 12:00:00, 1, 2024-03-13 12:00:00",
                "2024-02-13 12:00:00, -1, 2024-01-13 12:00:00",
                "2024-01-31 12:00:00, 1, 2024-02-29 12:00:00",
                "2024-12-31 12:00:00, 1, 2025-01-31 12:00:00"
        })
        @DisplayName("Should add months correctly")
        void shouldAddMonthsCorrectly(String startDateStr, int months, String expectedDateStr)
                throws Exception {
            Date startDate = SDF.parse(startDateStr);
            Date expectedDate = SDF.parse(expectedDateStr);

            Date result = DateHelper.addMonthsToDate(startDate, months);
            assertTrue(DateHelper.areDatesOnSameDay(expectedDate, result));
        }
    }
}