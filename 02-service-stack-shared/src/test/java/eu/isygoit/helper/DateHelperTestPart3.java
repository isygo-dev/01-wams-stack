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
 * The type Date helper test part 3.
 */
class DateHelperTestPart3 {

    /**
     * The constant SDF.
     */
    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * The type Week boundary tests.
     */
    @Nested
    @DisplayName("Week Boundary Tests")
    class WeekBoundaryTests {

        /**
         * Should get correct start of week.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should get correct start of week")
        void shouldGetCorrectStartOfWeek() throws Exception {
            // Test for each day of the week
            String[] weekDays = {
                    "2024-02-12", // Monday
                    "2024-02-13", // Tuesday
                    "2024-02-14", // Wednesday
                    "2024-02-15", // Thursday
                    "2024-02-16", // Friday
                    "2024-02-17", // Saturday
                    "2024-02-18"  // Sunday
            };

            SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");
            Date expectedMonday = dateSDF.parse("2024-02-12");

            for (String dayStr : weekDays) {
                Date day = dateSDF.parse(dayStr);
                Date result = DateHelper.getStartOfWeek(day);
                assertTrue(DateHelper.areDatesOnSameDay(expectedMonday, result),
                        "Start of week for " + dayStr + " should be Monday 2024-02-12");
            }
        }

        /**
         * Should get correct end of week.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should get correct end of week")
        void shouldGetCorrectEndOfWeek() throws Exception {
            // Test for each day of the week
            String[] weekDays = {
                    "2024-02-12", // Monday
                    "2024-02-13", // Tuesday
                    "2024-02-14", // Wednesday
                    "2024-02-15", // Thursday
                    "2024-02-16", // Friday
                    "2024-02-17", // Saturday
                    "2024-02-18"  // Sunday
            };

            SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");
            Date expectedSunday = dateSDF.parse("2024-02-18");

            for (String dayStr : weekDays) {
                Date day = dateSDF.parse(dayStr);
                Date result = DateHelper.getEndOfWeek(day);
                assertTrue(DateHelper.areDatesOnSameDay(expectedSunday, result),
                        "End of week for " + dayStr + " should be Sunday 2024-02-18");
            }
        }

        /**
         * Should handle week spanning month boundary.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should handle week spanning month boundary")
        void shouldHandleWeekSpanningMonthBoundary() throws Exception {
            SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");
            Date dateInWeek = dateSDF.parse("2024-01-31"); // Wednesday
            Date expectedMonday = dateSDF.parse("2024-01-29");
            Date expectedSunday = dateSDF.parse("2024-02-04");

            Date startResult = DateHelper.getStartOfWeek(dateInWeek);
            Date endResult = DateHelper.getEndOfWeek(dateInWeek);

            assertTrue(DateHelper.areDatesOnSameDay(expectedMonday, startResult));
            assertTrue(DateHelper.areDatesOnSameDay(expectedSunday, endResult));
        }

        /**
         * Should handle week spanning year boundary.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should handle week spanning year boundary")
        void shouldHandleWeekSpanningYearBoundary() throws Exception {
            SimpleDateFormat dateSDF = new SimpleDateFormat("yyyy-MM-dd");
            Date dateInWeek = dateSDF.parse("2024-12-31"); // Tuesday
            Date expectedMonday = dateSDF.parse("2024-12-30");
            Date expectedSunday = dateSDF.parse("2025-01-05");

            Date startResult = DateHelper.getStartOfWeek(dateInWeek);
            Date endResult = DateHelper.getEndOfWeek(dateInWeek);

            assertTrue(DateHelper.areDatesOnSameDay(expectedMonday, startResult));
            assertTrue(DateHelper.areDatesOnSameDay(expectedSunday, endResult));
        }
    }

    /**
     * The type Leap year tests.
     */
    @Nested
    @DisplayName("Leap Year Tests")
    class LeapYearTests {

        /**
         * Should identify leap years.
         *
         * @param year the year
         */
        @ParameterizedTest
        @ValueSource(ints = {2000, 2004, 2008, 2012, 2016, 2020, 2024, 2028})
        @DisplayName("Should identify leap years")
        void shouldIdentifyLeapYears(int year) {
            assertTrue(DateHelper.isLeapYear(year));
        }

        /**
         * Should identify non leap years.
         *
         * @param year the year
         */
        @ParameterizedTest
        @ValueSource(ints = {2001, 2002, 2003, 2005, 2006, 2007, 2009, 2010, 2011})
        @DisplayName("Should identify non-leap years")
        void shouldIdentifyNonLeapYears(int year) {
            assertFalse(DateHelper.isLeapYear(year));
        }

        /**
         * Should handle century years correctly.
         *
         * @param year the year
         */
        @ParameterizedTest
        @ValueSource(ints = {1900, 2100, 2200, 2300})
        @DisplayName("Should handle century years correctly")
        void shouldHandleCenturyYearsCorrectly(int year) {
            assertFalse(DateHelper.isLeapYear(year));
        }

        /**
         * Should handle century leap years correctly.
         *
         * @param year the year
         */
        @ParameterizedTest
        @ValueSource(ints = {2000, 2400})
        @DisplayName("Should handle century leap years correctly")
        void shouldHandleCenturyLeapYearsCorrectly(int year) {
            assertTrue(DateHelper.isLeapYear(year));
        }
    }

    /**
     * The type Human readable format tests.
     */
    @Nested
    @DisplayName("Human Readable Format Tests")
    class HumanReadableFormatTests {

        /**
         * Should format dates in human readable format.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should format dates in human readable format")
        void shouldFormatDatesInHumanReadableFormat() throws Exception {
            String[][] testCases = {
                    {"2024-02-13 12:00:00", "February 13, 2024"},
                    {"2024-01-01 00:00:00", "January 01, 2024"},
                    {"2024-12-31 23:59:59", "December 31, 2024"},
                    {"2024-07-04 15:30:00", "July 04, 2024"},
                    {"2024-09-30 09:15:00", "September 30, 2024"}
            };

            for (String[] testCase : testCases) {
                Date date = SDF.parse(testCase[0]);
                String expected = testCase[1];
                String result = DateHelper.formatDateToHumanReadable(date);
                assertEquals(expected, result);
            }
        }

        /**
         * Should handle leap year date.
         *
         * @throws Exception the exception
         */
        @Test
        @DisplayName("Should handle leap year date")
        void shouldHandleLeapYearDate() throws Exception {
            Date leapDate = SDF.parse("2024-02-29 12:00:00");
            String result = DateHelper.formatDateToHumanReadable(leapDate);
            assertEquals("February 29, 2024", result);
        }

        /**
         * Should throw exception for null date.
         */
        @Test
        @DisplayName("Should throw exception for null date")
        void shouldThrowExceptionForNullDate() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.formatDateToHumanReadable(null));
        }
    }

    /**
     * The type Null handling tests.
     */
    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        /**
         * Should verify all null checks.
         */
        @Test
        @DisplayName("Should verify all null checks")
        void shouldVerifyAllNullChecks() {
            // Test all methods that should throw NullPointerException
            assertThrows(NullPointerException.class, () -> DateHelper.occurredInLastXHours(null, 1));
            assertThrows(NullPointerException.class, () -> DateHelper.areDatesOnSameDay(null, new Date()));
            assertThrows(NullPointerException.class, () -> DateHelper.areDatesOnSameDay(new Date(), null));
            assertThrows(NullPointerException.class, () -> DateHelper.addDaysToDate(null, 1));
            assertThrows(NullPointerException.class, () -> DateHelper.addMonthsToDate(null, 1));
            assertThrows(NullPointerException.class, () -> DateHelper.getStartOfWeek(null));
            assertThrows(NullPointerException.class, () -> DateHelper.getEndOfWeek(null));
            assertThrows(NullPointerException.class, () -> DateHelper.formatDateToHumanReadable(null));
        }
    }
}