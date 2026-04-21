package eu.isygoit.helper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for DateHelper interface.
 * Tests cover both deprecated java.util.Date methods and modern LocalDate/LocalDateTime methods.
 */
@DisplayName("DateHelper Comprehensive Tests")
class DateHelperTest {

    public static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_SDF = new SimpleDateFormat("yyyy-MM-dd");

    // ==================== PARSING TESTS ====================

    @Nested
    @DisplayName("Parsing Tests - LocalDateTime")
    class ParsingLocalDateTimeTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "2024-02-13T15:30:45.123+01:00",
                "2024-02-13T15:30:45+01:00",
                "2024-02-13T15:30",
                "2024-02-13",
                "2024/02/13",
                "13-02-2024T15:30:45.123+01:00",
                "13-02-2024",
                "13/02/2024",
                "13/02/2024 15:30",
                "20240213",
                "13022024",
                "202402131530",
                "20240213153045",
                "02-13-2024",
                "02/13/2024",
                "February 13, 2024",
                "2024-02-13"
        })
        @DisplayName("Should parse various valid date formats to LocalDateTime")
        void shouldParseValidDateFormatsToLocalDateTime(String dateString) {
            LocalDateTime result = DateHelper.parseLocalDateTime(dateString, null);
            assertNotNull(result, "Failed to parse: " + dateString);
            assertTrue(result.getYear() == 2024 && result.getMonthValue() == 2 && result.getDayOfMonth() == 13,
                    "Parsed date should be 2024-02-13 for input: " + dateString);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "null", "NULL"})
        @DisplayName("Should return default for empty or null LocalDateTime strings")
        void shouldReturnDefaultForEmptyOrNullLocalDateTime(String dateString) {
            LocalDateTime defaultDateTime = LocalDateTime.of(2020, 1, 1, 0, 0);
            assertEquals(defaultDateTime, DateHelper.parseLocalDateTime(dateString, defaultDateTime));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "invalid-date",
                "2024-13-13",
                "2024-02-31",
                "abc",
                "13/13/2024",
                "29/02/2023",
                "31/04/2024",
                "31/06/2024",
                "31/09/2024",
                "31/11/2024"
        })
        @DisplayName("Should throw exception for invalid date formats in LocalDateTime parsing")
        void shouldThrowExceptionForInvalidFormatsLocalDateTime(String dateString) {
            assertThrows(IllegalArgumentException.class,
                    () -> DateHelper.parseLocalDateTime(dateString, null));
        }

        @Test
        @DisplayName("Should parse epoch and epoch_second to LocalDateTime")
        void shouldParseEpochAndEpochSecondLocalDateTime() {
            long epochMillis = 1707838245123L;
            LocalDateTime result = DateHelper.parseLocalDateTime(String.valueOf(epochMillis), null);
            assertNotNull(result);

            long epochSeconds = 1707838245L;
            LocalDateTime result2 = DateHelper.parseLocalDateTime(String.valueOf(epochSeconds), null);
            assertNotNull(result2);
        }
    }

    @Nested
    @DisplayName("Parsing Tests - LocalDate")
    class ParsingLocalDateTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "2024-02-13",
                "2024/02/13",
                "13-02-2024",
                "13/02/2024",
                "20240213",
                "13022024",
                "02-13-2024",
                "02/13/2024",
                "February 13, 2024"
        })
        @DisplayName("Should parse date formats to LocalDate")
        void shouldParseValidDateFormatsToLocalDate(String dateString) {
            LocalDate result = DateHelper.parseLocalDate(dateString, null);
            assertNotNull(result);
            assertEquals(2024, result.getYear());
            assertEquals(2, result.getMonthValue());
            assertEquals(13, result.getDayOfMonth());
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "null", "NULL"})
        @DisplayName("Should return default for empty or null LocalDate strings")
        void shouldReturnDefaultForEmptyOrNullLocalDate(String dateString) {
            LocalDate defaultDate = LocalDate.of(2020, 1, 1);
            assertEquals(defaultDate, DateHelper.parseLocalDate(dateString, defaultDate));
        }
    }

    @Nested
    @DisplayName("Parsing Tests - Deprecated Date")
    class ParsingDateTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "2024-02-13T15:30:45.123+01:00",
                "2024-02-13T15:30:45+01:00",
                "2024-02-13T15:30",
                "2024-02-13",
                "2024/02/13",
                "13-02-2024",
                "13/02/2024",
                "20240213",
                "02-13-2024",
                "02/13/2024"
        })
        @DisplayName("Should parse valid date formats to Date (deprecated)")
        void shouldParseValidDateFormatsToDate(String dateString) {
            @SuppressWarnings("deprecation")
            Date result = DateHelper.parseDateString(dateString, null);
            assertNotNull(result);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "null", "NULL"})
        @DisplayName("Should return default for empty or null Date strings (deprecated)")
        void shouldReturnDefaultForEmptyOrNullDate(String dateString) {
            Date defaultDate = new Date();
            @SuppressWarnings("deprecation")
            Date result = DateHelper.parseDateString(dateString, defaultDate);
            assertEquals(defaultDate, result);
        }
    }

    // ==================== FORMATTING TESTS ====================

    @Nested
    @DisplayName("Formatting Tests - LocalDateTime ISO")
    class FormattingLocalDateTimeIsoTests {

        @Test
        @DisplayName("Should format LocalDateTime to ISO string")
        void shouldFormatLocalDateTimeToIsoString() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            String result = DateHelper.formatToIsoString(dateTime);

            assertNotNull(result);
            assertTrue(result.contains("T"));
            assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
        }

        @Test
        @DisplayName("Should handle null LocalDateTime")
        void shouldHandleNullLocalDateTime() {
            assertNull(DateHelper.formatToIsoString((LocalDateTime) null));
        }
    }

    @Nested
    @DisplayName("Formatting Tests - LocalDate ISO")
    class FormattingLocalDateIsoTests {

        @Test
        @DisplayName("Should format LocalDate to ISO string")
        void shouldFormatLocalDateToIsoString() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            String result = DateHelper.formatToIsoString(date);

            assertNotNull(result);
            assertEquals("2024-02-13", result);
        }

        @Test
        @DisplayName("Should handle null LocalDate")
        void shouldHandleNullLocalDate() {
            assertNull(DateHelper.formatToIsoString((LocalDate) null));
        }
    }

    @Nested
    @DisplayName("Formatting Tests - Human Readable")
    class FormattingHumanReadableTests {

        @Test
        @DisplayName("Should format LocalDateTime in human readable format")
        void shouldFormatLocalDateTimeToHumanReadable() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            String result = DateHelper.formatToHumanReadable(dateTime);

            assertNotNull(result);
            assertTrue(result.contains("February"));
            assertTrue(result.contains("2024"));
            assertEquals("February 13, 2024 15:30:45", result);
        }

        @Test
        @DisplayName("Should format LocalDate in human readable format")
        void shouldFormatLocalDateToHumanReadable() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            String result = DateHelper.formatToHumanReadable(date);

            assertNotNull(result);
            assertTrue(result.contains("February"));
            assertTrue(result.contains("2024"));
            assertEquals("February 13, 2024", result);
        }

        @Test
        @DisplayName("Should throw exception for null LocalDateTime")
        void shouldThrowExceptionForNullLocalDateTime() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.formatToHumanReadable((LocalDateTime) null));
        }

        @Test
        @DisplayName("Should throw exception for null LocalDate")
        void shouldThrowExceptionForNullLocalDate() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.formatToHumanReadable((LocalDate) null));
        }
    }

    @Nested
    @DisplayName("Formatting Tests - Deprecated Date ISO")
    class FormattingDateIsoTests {

        @Test
        @DisplayName("Should format Date to ISO string (deprecated)")
        void shouldFormatDateToIsoString() throws Exception {
            Date date = SDF.parse("2024-02-13 15:30:45");
            @SuppressWarnings("deprecation")
            String result = DateHelper.formatDateToIsoString(date);

            assertNotNull(result);
            assertTrue(result.contains("T"));
            assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*"));
        }

        @Test
        @DisplayName("Should handle null Date in ISO formatting (deprecated)")
        void shouldHandleNullDateInIsoFormatting() {
            @SuppressWarnings("deprecation")
            String result = DateHelper.formatDateToIsoString(null);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Formatting Tests - Deprecated Date Human Readable")
    class FormattingDateHumanReadableTests {

        @Test
        @DisplayName("Should format Date in human readable format (deprecated)")
        void shouldFormatDateToHumanReadable() throws Exception {
            Date date = SDF.parse("2024-02-13 12:00:00");
            @SuppressWarnings("deprecation")
            String result = DateHelper.formatDateToHumanReadable(date);

            assertEquals("February 13, 2024", result);
        }

        @Test
        @DisplayName("Should handle leap year date (deprecated)")
        void shouldHandleLeapYearDate() throws Exception {
            Date leapDate = SDF.parse("2024-02-29 12:00:00");
            @SuppressWarnings("deprecation")
            String result = DateHelper.formatDateToHumanReadable(leapDate);
            assertEquals("February 29, 2024", result);
        }
    }

    // ==================== TIME COMPARISON TESTS ====================

    @Nested
    @DisplayName("Occurred in Last X Hours Tests - LocalDateTime")
    class OccurredInLastXHoursLocalDateTimeTests {

        @Test
        @DisplayName("Should handle various time ranges for LocalDateTime")
        void shouldHandleVariousTimeRanges() {
            LocalDateTime now = LocalDateTime.now();

            int[] hourRanges = {1, 6, 12, 24, 48, 168};
            for (int hours : hourRanges) {
                assertTrue(DateHelper.occurredInLastXHours(now, hours));

                LocalDateTime pastDateTime = now.minus(hours + 1, ChronoUnit.HOURS);
                assertFalse(DateHelper.occurredInLastXHours(pastDateTime, hours));
            }
        }

        @Test
        @DisplayName("Should handle edge cases for LocalDateTime")
        void shouldHandleEdgeCases() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime exactBoundary = now.minus(24, ChronoUnit.HOURS);
            assertFalse(DateHelper.occurredInLastXHours(exactBoundary, 24));

            LocalDateTime nearBoundary = now.minus(23, ChronoUnit.HOURS).minus(59, ChronoUnit.MINUTES);
            assertTrue(DateHelper.occurredInLastXHours(nearBoundary, 24));
        }

        @Test
        @DisplayName("Should handle future LocalDateTime")
        void shouldHandleFutureDateTime() {
            LocalDateTime futureDateTime = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
            assertTrue(DateHelper.occurredInLastXHours(futureDateTime, 24));
        }
    }

    @Nested
    @DisplayName("Occurred in Last X Hours Tests - Deprecated Date")
    class OccurredInLastXHoursDateTests {

        @Test
        @DisplayName("Should handle various time ranges for Date (deprecated)")
        void shouldHandleVariousTimeRanges() {
            Date now = new Date();

            int[] hourRanges = {1, 6, 12, 24, 48, 168};
            for (int hours : hourRanges) {
                @SuppressWarnings("deprecation")
                boolean isRecent = DateHelper.occurredInLastXHours(now, hours);
                assertTrue(isRecent);

                Date pastDate = Date.from(Instant.now().minus(Duration.ofHours(hours + 1)));
                @SuppressWarnings("deprecation")
                boolean isOld = DateHelper.occurredInLastXHours(pastDate, hours);
                assertFalse(isOld);
            }
        }
    }

    // ==================== SAME DAY TESTS ====================

    @Nested
    @DisplayName("Same Day Tests - LocalDate")
    class SameDayLocalDateTests {

        @ParameterizedTest
        @CsvSource({
                "2024-02-13, 2024-02-13, true",
                "2024-02-13, 2024-02-14, false",
                "2024-02-13, 2024-01-13, false",
                "2024-12-31, 2025-01-01, false"
        })
        @DisplayName("Should compare LocalDates correctly")
        void shouldCompareDatescorrectly(String date1Str, String date2Str, boolean expected) {
            LocalDate date1 = LocalDate.parse(date1Str);
            LocalDate date2 = LocalDate.parse(date2Str);

            assertEquals(expected, DateHelper.areDatesOnSameDay(date1, date2));
        }
    }

    @Nested
    @DisplayName("Same Day Tests - LocalDateTime")
    class SameDayLocalDateTimeTests {

        @ParameterizedTest
        @CsvSource({
                "2024-02-13T00:00:00, 2024-02-13T23:59:59, true",
                "2024-02-13T12:00:00, 2024-02-13T15:30:00, true",
                "2024-02-13T00:00:00, 2024-02-14T00:00:00, false",
                "2024-12-31T23:59:59, 2025-01-01T00:00:00, false"
        })
        @DisplayName("Should compare LocalDateTimes correctly")
        void shouldCompareDateTimescorrectly(String dateTime1Str, String dateTime2Str, boolean expected) {
            LocalDateTime dateTime1 = LocalDateTime.parse(dateTime1Str);
            LocalDateTime dateTime2 = LocalDateTime.parse(dateTime2Str);

            assertEquals(expected, DateHelper.areDatesOnSameDay(dateTime1, dateTime2));
        }
    }

    @Nested
    @DisplayName("Same Day Tests - Deprecated Date")
    class SameDayDateTests {

        @ParameterizedTest
        @CsvSource({
                "2024-02-13 00:00:00, 2024-02-13 23:59:59, true",
                "2024-02-13 00:00:00, 2024-02-14 00:00:00, false",
                "2024-02-13 12:00:00, 2024-02-13 15:30:00, true",
                "2024-12-31 23:59:59, 2025-01-01 00:00:00, false"
        })
        @DisplayName("Should compare Dates correctly (deprecated)")
        void shouldCompareDatescorrectly(String date1Str, String date2Str, boolean expected)
                throws Exception {
            Date date1 = SDF.parse(date1Str);
            Date date2 = SDF.parse(date2Str);

            @SuppressWarnings("deprecation")
            boolean result = DateHelper.areDatesOnSameDay(date1, date2);
            assertEquals(expected, result);
        }

        @Test
        @DisplayName("Should handle leap year boundary (deprecated)")
        void shouldHandleLeapYearBoundary() throws Exception {
            Date leapYearEnd = SDF.parse("2024-02-29 23:59:59");
            Date marchFirst = SDF.parse("2024-03-01 00:00:00");
            @SuppressWarnings("deprecation")
            boolean result = DateHelper.areDatesOnSameDay(leapYearEnd, marchFirst);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle year boundary (deprecated)")
        void shouldHandleYearBoundary() throws Exception {
            Date yearEnd = SDF.parse("2024-12-31 23:59:59");
            Date yearStart = SDF.parse("2025-01-01 00:00:00");
            @SuppressWarnings("deprecation")
            boolean result = DateHelper.areDatesOnSameDay(yearEnd, yearStart);
            assertFalse(result);
        }
    }

    // ==================== DATE ADDITION TESTS ====================

    @Nested
    @DisplayName("Date Addition Tests - LocalDate")
    class DateAdditionLocalDateTests {

        @ParameterizedTest
        @CsvSource({
                "2024-02-13, 1, 2024-02-14",
                "2024-02-13, -1, 2024-02-12",
                "2024-02-28, 1, 2024-02-29",
                "2024-12-31, 1, 2025-01-01",
                "2024-02-13, 7, 2024-02-20",
                "2024-02-13, -7, 2024-02-06"
        })
        @DisplayName("Should add days to LocalDate correctly")
        void shouldAddDaysCorrectly(String startDateStr, int days, String expectedDateStr) {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate expectedDate = LocalDate.parse(expectedDateStr);

            LocalDate result = DateHelper.addDays(startDate, days);
            assertEquals(expectedDate, result);
        }

        @ParameterizedTest
        @CsvSource({
                "2024-02-13, 1, 2024-03-13",
                "2024-02-13, -1, 2024-01-13",
                "2024-01-31, 1, 2024-02-29",
                "2024-12-31, 1, 2025-01-31",
                "2024-02-13, 12, 2025-02-13"
        })
        @DisplayName("Should add months to LocalDate correctly")
        void shouldAddMonthsCorrectly(String startDateStr, int months, String expectedDateStr) {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate expectedDate = LocalDate.parse(expectedDateStr);

            LocalDate result = DateHelper.addMonths(startDate, months);
            assertEquals(expectedDate, result);
        }

        @ParameterizedTest
        @CsvSource({
                "2024-02-13, 1, 2025-02-13",
                "2024-02-13, -1, 2023-02-13",
                "2024-02-13, 10, 2034-02-13"
        })
        @DisplayName("Should add years to LocalDate correctly")
        void shouldAddYearsCorrectly(String startDateStr, int years, String expectedDateStr) {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate expectedDate = LocalDate.parse(expectedDateStr);

            LocalDate result = DateHelper.addYears(startDate, years);
            assertEquals(expectedDate, result);
        }
    }

    @Nested
    @DisplayName("Date Addition Tests - LocalDateTime")
    class DateAdditionLocalDateTimeTests {

        @Test
        @DisplayName("Should add days to LocalDateTime correctly")
        void shouldAddDaysToDateTime() {
            LocalDateTime startDateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.addDays(startDateTime, 1);

            assertEquals(LocalDateTime.of(2024, 2, 14, 15, 30, 45), result);
        }

        @Test
        @DisplayName("Should add months to LocalDateTime correctly")
        void shouldAddMonthsToDateTime() {
            LocalDateTime startDateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.addMonths(startDateTime, 1);

            assertEquals(LocalDateTime.of(2024, 3, 13, 15, 30, 45), result);
        }

        @Test
        @DisplayName("Should add years to LocalDateTime correctly")
        void shouldAddYearsToDateTime() {
            LocalDateTime startDateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.addYears(startDateTime, 1);

            assertEquals(LocalDateTime.of(2025, 2, 13, 15, 30, 45), result);
        }
    }

    @Nested
    @DisplayName("Date Addition Tests - Deprecated Date")
    class DateAdditionDateTests {

        @ParameterizedTest
        @CsvSource({
                "2024-02-13 12:00:00, 1, 2024-02-14",
                "2024-02-13 12:00:00, -1, 2024-02-12",
                "2024-02-28 12:00:00, 1, 2024-02-29",
                "2024-12-31 12:00:00, 1, 2025-01-01"
        })
        @DisplayName("Should add days to Date correctly (deprecated)")
        void shouldAddDaysCorrectly(String startDateStr, int days, String expectedDateStr)
                throws Exception {
            Date startDate = SDF.parse(startDateStr);
            LocalDate expectedDate = LocalDate.parse(expectedDateStr);

            @SuppressWarnings("deprecation")
            Date result = DateHelper.addDaysToDate(startDate, days);
            LocalDate resultDate = DateHelper.legacyDatetoLocalDate(result);
            assertEquals(expectedDate, resultDate);
        }

        @ParameterizedTest
        @CsvSource({
                "2024-02-13 12:00:00, 1, 2024-03-13",
                "2024-02-13 12:00:00, -1, 2024-01-13",
                "2024-01-31 12:00:00, 1, 2024-02-29",
                "2024-12-31 12:00:00, 1, 2025-01-31"
        })
        @DisplayName("Should add months to Date correctly (deprecated)")
        void shouldAddMonthsCorrectly(String startDateStr, int months, String expectedDateStr)
                throws Exception {
            Date startDate = SDF.parse(startDateStr);
            LocalDate expectedDate = LocalDate.parse(expectedDateStr);

            @SuppressWarnings("deprecation")
            Date result = DateHelper.addMonthsToDate(startDate, months);
            LocalDate resultDate = DateHelper.legacyDatetoLocalDate(result);
            assertEquals(expectedDate, resultDate);
        }
    }

    // ==================== WEEK BOUNDARY TESTS ====================

    @Nested
    @DisplayName("Week Boundary Tests - LocalDate")
    class WeekBoundaryLocalDateTests {

        @Test
        @DisplayName("Should get correct start of week for LocalDate")
        void shouldGetCorrectStartOfWeekLocalDate() {
            String[] weekDays = {
                    "2024-02-12", // Monday
                    "2024-02-13", // Tuesday
                    "2024-02-14", // Wednesday
                    "2024-02-15", // Thursday
                    "2024-02-16", // Friday
                    "2024-02-17", // Saturday
                    "2024-02-18"  // Sunday
            };

            LocalDate expectedMonday = LocalDate.of(2024, 2, 12);

            for (String dayStr : weekDays) {
                LocalDate day = LocalDate.parse(dayStr);
                LocalDate result = DateHelper.getStartOfWeek(day);
                assertEquals(expectedMonday, result, "Start of week for " + dayStr + " should be Monday");
            }
        }

        @Test
        @DisplayName("Should get correct end of week for LocalDate")
        void shouldGetCorrectEndOfWeekLocalDate() {
            String[] weekDays = {
                    "2024-02-12", // Monday
                    "2024-02-13", // Tuesday
                    "2024-02-14", // Wednesday
                    "2024-02-15", // Thursday
                    "2024-02-16", // Friday
                    "2024-02-17", // Saturday
                    "2024-02-18"  // Sunday
            };

            LocalDate expectedSunday = LocalDate.of(2024, 2, 18);

            for (String dayStr : weekDays) {
                LocalDate day = LocalDate.parse(dayStr);
                LocalDate result = DateHelper.getEndOfWeek(day);
                assertEquals(expectedSunday, result, "End of week for " + dayStr + " should be Sunday");
            }
        }
    }

    @Nested
    @DisplayName("Week Boundary Tests - LocalDateTime")
    class WeekBoundaryLocalDateTimeTests {

        @Test
        @DisplayName("Should get correct start of week for LocalDateTime")
        void shouldGetCorrectStartOfWeekLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.getStartOfWeek(dateTime);

            assertEquals(DayOfWeek.MONDAY, result.getDayOfWeek());
            assertEquals(2024, result.getYear());
            assertEquals(2, result.getMonthValue());
            assertEquals(12, result.getDayOfMonth());
            assertEquals(0, result.getHour());
            assertEquals(0, result.getMinute());
            assertEquals(0, result.getSecond());
        }

        @Test
        @DisplayName("Should get correct end of week for LocalDateTime")
        void shouldGetCorrectEndOfWeekLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.getEndOfWeek(dateTime);

            assertEquals(DayOfWeek.SUNDAY, result.getDayOfWeek());
            assertEquals(2024, result.getYear());
            assertEquals(2, result.getMonthValue());
            assertEquals(18, result.getDayOfMonth());
            assertEquals(23, result.getHour());
            assertEquals(59, result.getMinute());
            assertEquals(59, result.getSecond());
        }
    }

    @Nested
    @DisplayName("Week Boundary Tests - Deprecated Date")
    class WeekBoundaryDateTests {

        @Test
        @DisplayName("Should get correct start of week for Date (deprecated)")
        void shouldGetCorrectStartOfWeekDate() throws Exception {
            LocalDate expectedMonday = LocalDate.of(2024, 2, 12);

            for (int i = 0; i < 7; i++) {
                LocalDate day = expectedMonday.plusDays(i);
                Date dayAsDate = DateHelper.toLegacyDate(day);
                @SuppressWarnings("deprecation")
                Date result = DateHelper.getStartOfWeek(dayAsDate);
                LocalDate resultDate = DateHelper.legacyDatetoLocalDate(result);
                assertEquals(expectedMonday, resultDate);
            }
        }

        @Test
        @DisplayName("Should get correct end of week for Date (deprecated)")
        void shouldGetCorrectEndOfWeekDate() throws Exception {
            LocalDate expectedSunday = LocalDate.of(2024, 2, 18);

            for (int i = 0; i < 7; i++) {
                LocalDate day = expectedSunday.minusDays(6 - i);
                Date dayAsDate = DateHelper.toLegacyDate(day);
                @SuppressWarnings("deprecation")
                Date result = DateHelper.getEndOfWeek(dayAsDate);
                LocalDate resultDate = DateHelper.legacyDatetoLocalDate(result);
                assertEquals(expectedSunday, resultDate);
            }
        }
    }

    // ==================== MONTH BOUNDARY TESTS ====================

    @Nested
    @DisplayName("Month Boundary Tests - LocalDate")
    class MonthBoundaryLocalDateTests {

        @Test
        @DisplayName("Should get correct start of month for LocalDate")
        void shouldGetCorrectStartOfMonth() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            LocalDate result = DateHelper.getStartOfMonth(date);

            assertEquals(LocalDate.of(2024, 2, 1), result);
        }

        @Test
        @DisplayName("Should get correct end of month for LocalDate")
        void shouldGetCorrectEndOfMonth() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            LocalDate result = DateHelper.getEndOfMonth(date);

            assertEquals(LocalDate.of(2024, 2, 29), result); // Leap year
        }

        @Test
        @DisplayName("Should get correct end of month for non-leap year")
        void shouldGetCorrectEndOfMonthNonLeap() {
            LocalDate date = LocalDate.of(2023, 2, 13);
            LocalDate result = DateHelper.getEndOfMonth(date);

            assertEquals(LocalDate.of(2023, 2, 28), result);
        }
    }

    @Nested
    @DisplayName("Month Boundary Tests - LocalDateTime")
    class MonthBoundaryLocalDateTimeTests {

        @Test
        @DisplayName("Should get correct start of month for LocalDateTime")
        void shouldGetCorrectStartOfMonthDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.getStartOfMonth(dateTime);

            assertEquals(LocalDateTime.of(2024, 2, 1, 0, 0, 0), result);
        }

        @Test
        @DisplayName("Should get correct end of month for LocalDateTime")
        void shouldGetCorrectEndOfMonthDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            LocalDateTime result = DateHelper.getEndOfMonth(dateTime);

            assertEquals(23, result.getHour());
            assertEquals(59, result.getMinute());
            assertEquals(59, result.getSecond());
            assertEquals(29, result.getDayOfMonth()); // Leap year
        }
    }

    // ==================== TODAY TESTS ====================

    @Nested
    @DisplayName("Today Tests")
    class TodayTests {

        @Test
        @DisplayName("Should identify today for LocalDate")
        void shouldIdentifyTodayLocalDate() {
            LocalDate today = LocalDate.now();
            assertTrue(DateHelper.isToday(today));

            LocalDate tomorrow = today.plusDays(1);
            assertFalse(DateHelper.isToday(tomorrow));
        }

        @Test
        @DisplayName("Should identify today for LocalDateTime")
        void shouldIdentifyTodayLocalDateTime() {
            LocalDateTime now = LocalDateTime.now();
            assertTrue(DateHelper.isToday(now));

            LocalDateTime tomorrow = now.plusDays(1);
            assertFalse(DateHelper.isToday(tomorrow));
        }

        @Test
        @DisplayName("Should identify today for Date (deprecated)")
        void shouldIdentifyTodayDate() {
            Date today = new Date();
            @SuppressWarnings("deprecation")
            boolean result = DateHelper.isToday(today);
            assertTrue(result);
        }
    }

    // ==================== PAST/FUTURE TESTS ====================

    @Nested
    @DisplayName("Past/Future Tests - LocalDate")
    class PastFutureLocalDateTests {

        @Test
        @DisplayName("Should identify past LocalDate")
        void shouldIdentifyPastDate() {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            assertTrue(DateHelper.isPast(yesterday));

            LocalDate today = LocalDate.now();
            assertFalse(DateHelper.isPast(today));
        }

        @Test
        @DisplayName("Should identify future LocalDate")
        void shouldIdentifyFutureDate() {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            assertTrue(DateHelper.isFuture(tomorrow));

            LocalDate today = LocalDate.now();
            assertFalse(DateHelper.isFuture(today));
        }
    }

    @Nested
    @DisplayName("Past/Future Tests - LocalDateTime")
    class PastFutureLocalDateTimeTests {

        @Test
        @DisplayName("Should identify past LocalDateTime")
        void shouldIdentifyPastDateTime() {
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            assertTrue(DateHelper.isPast(pastTime));

            LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
            assertFalse(DateHelper.isPast(futureTime));
        }

        @Test
        @DisplayName("Should identify future LocalDateTime")
        void shouldIdentifyFutureDateTime() {
            LocalDateTime futureTime = LocalDateTime.now().plusHours(1);
            assertTrue(DateHelper.isFuture(futureTime));

            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            assertFalse(DateHelper.isFuture(pastTime));
        }
    }

    // ==================== CLEAR TIME TESTS ====================

    @Nested
    @DisplayName("Clear Time Tests")
    class ClearTimeTests {

        @Test
        @DisplayName("Should clear time from LocalDateTime")
        void shouldClearTimeFromLocalDateTime() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45, 123456);
            LocalDateTime result = DateHelper.clearTime(dateTime);

            assertEquals(LocalDateTime.of(2024, 2, 13, 0, 0, 0, 0), result);
        }

        @Test
        @DisplayName("Should return same LocalDate when clearing time")
        void shouldReturnSameLocalDateWhenClearingTime() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            LocalDate result = DateHelper.clearTime(date);

            assertEquals(date, result);
        }

        @Test
        @DisplayName("Should return null for null LocalDateTime")
        void shouldReturnNullForNullLocalDateTime() {
            assertNull(DateHelper.clearTime((LocalDateTime) null));
        }

        @Test
        @DisplayName("Should return null for null LocalDate")
        void shouldReturnNullForNullLocalDate() {
            assertNull(DateHelper.clearTime((LocalDate) null));
        }

        @Test
        @DisplayName("Should clear time from Date (deprecated)")
        void shouldClearTimeFromDate() throws Exception {
            Date dateWithTime = SDF.parse("2024-02-13 15:30:45");
            @SuppressWarnings("deprecation")
            Date result = DateHelper.clearTime(dateWithTime);

            LocalDateTime resultDateTime = DateHelper.toLocalDateTime(result);
            assertEquals(0, resultDateTime.getHour());
            assertEquals(0, resultDateTime.getMinute());
            assertEquals(0, resultDateTime.getSecond());
        }
    }

    // ==================== HAS TIME TESTS ====================

    @Nested
    @DisplayName("Has Time Tests")
    class HasTimeTests {

        @Test
        @DisplayName("Should detect time in LocalDateTime")
        void shouldDetectTimeInLocalDateTime() {
            LocalDateTime withTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            assertTrue(DateHelper.hasTime(withTime));

            LocalDateTime noTime = LocalDateTime.of(2024, 2, 13, 0, 0, 0);
            assertFalse(DateHelper.hasTime(noTime));
        }

        @Test
        @DisplayName("Should always return false for LocalDate")
        void shouldAlwaysReturnFalseForLocalDate() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            assertFalse(DateHelper.hasTime(date));
        }

        @Test
        @DisplayName("Should return false for null LocalDateTime")
        void shouldReturnFalseForNullLocalDateTime() {
            assertFalse(DateHelper.hasTime((LocalDateTime) null));
        }

        @Test
        @DisplayName("Should return false for null LocalDate")
        void shouldReturnFalseForNullLocalDate() {
            assertFalse(DateHelper.hasTime((LocalDate) null));
        }

        @Test
        @DisplayName("Should detect time in Date (deprecated)")
        void shouldDetectTimeInDate() throws Exception {
            Date withTime = SDF.parse("2024-02-13 15:30:45");
            @SuppressWarnings("deprecation")
            boolean hasTime = DateHelper.hasTime(withTime);
            assertTrue(hasTime);

            Date noTime = SDF.parse("2024-02-13 00:00:00");
            @SuppressWarnings("deprecation")
            boolean hasNoTime = DateHelper.hasTime(noTime);
            assertFalse(hasNoTime);
        }
    }

    // ==================== MIN/MAX TESTS ====================

    @Nested
    @DisplayName("Min/Max Tests - LocalDate")
    class MinMaxLocalDateTests {

        @Test
        @DisplayName("Should find max LocalDate")
        void shouldFindMaxLocalDate() {
            LocalDate date1 = LocalDate.of(2024, 2, 13);
            LocalDate date2 = LocalDate.of(2024, 2, 20);

            LocalDate result = DateHelper.max(date1, date2);
            assertEquals(date2, result);
        }

        @Test
        @DisplayName("Should find min LocalDate")
        void shouldFindMinLocalDate() {
            LocalDate date1 = LocalDate.of(2024, 2, 13);
            LocalDate date2 = LocalDate.of(2024, 2, 20);

            LocalDate result = DateHelper.min(date1, date2);
            assertEquals(date1, result);
        }

        @Test
        @DisplayName("Should handle null in max LocalDate")
        void shouldHandleNullInMaxLocalDate() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            assertEquals(date, DateHelper.max(null, date));
            assertEquals(date, DateHelper.max(date, null));
        }

        @Test
        @DisplayName("Should handle null in min LocalDate")
        void shouldHandleNullInMinLocalDate() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            assertEquals(date, DateHelper.min(null, date));
            assertEquals(date, DateHelper.min(date, null));
        }
    }

    @Nested
    @DisplayName("Min/Max Tests - LocalDateTime")
    class MinMaxLocalDateTimeTests {

        @Test
        @DisplayName("Should find max LocalDateTime")
        void shouldFindMaxLocalDateTime() {
            LocalDateTime dateTime1 = LocalDateTime.of(2024, 2, 13, 10, 0);
            LocalDateTime dateTime2 = LocalDateTime.of(2024, 2, 13, 15, 0);

            LocalDateTime result = DateHelper.max(dateTime1, dateTime2);
            assertEquals(dateTime2, result);
        }

        @Test
        @DisplayName("Should find min LocalDateTime")
        void shouldFindMinLocalDateTime() {
            LocalDateTime dateTime1 = LocalDateTime.of(2024, 2, 13, 10, 0);
            LocalDateTime dateTime2 = LocalDateTime.of(2024, 2, 13, 15, 0);

            LocalDateTime result = DateHelper.min(dateTime1, dateTime2);
            assertEquals(dateTime1, result);
        }
    }

    @Nested
    @DisplayName("Min/Max Tests - Deprecated Date")
    class MinMaxDateTests {

        @Test
        @DisplayName("Should find max Date (deprecated)")
        void shouldFindMaxDate() throws Exception {
            Date date1 = SDF.parse("2024-02-13 10:00:00");
            Date date2 = SDF.parse("2024-02-13 15:00:00");

            @SuppressWarnings("deprecation")
            Date result = DateHelper.max(date1, date2);
            assertEquals(date2, result);
        }

        @Test
        @DisplayName("Should find min Date (deprecated)")
        void shouldFindMinDate() throws Exception {
            Date date1 = SDF.parse("2024-02-13 10:00:00");
            Date date2 = SDF.parse("2024-02-13 15:00:00");

            @SuppressWarnings("deprecation")
            Date result = DateHelper.min(date1, date2);
            assertEquals(date1, result);
        }
    }

    // ==================== BETWEEN TESTS ====================

    @Nested
    @DisplayName("Between Tests - Seconds")
    class BetweenSecondsTests {

        @Test
        @DisplayName("Should calculate seconds between LocalDateTime")
        void shouldCalculateSecondsBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 13, 10, 1, 0);

            long result = DateHelper.secondsBetween(start, end);
            assertEquals(60, result);
        }

        @Test
        @DisplayName("Should handle negative seconds between")
        void shouldHandleNegativeSecondsBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 13, 9, 59, 0);

            long result = DateHelper.secondsBetween(start, end);
            assertEquals(-60, result);
        }
    }

    @Nested
    @DisplayName("Between Tests - Hours")
    class BetweenHoursTests {

        @Test
        @DisplayName("Should calculate hours between LocalDateTime")
        void shouldCalculateHoursBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 13, 13, 0, 0);

            long result = DateHelper.hoursBetween(start, end);
            assertEquals(3, result);
        }
    }

    @Nested
    @DisplayName("Between Tests - Minutes")
    class BetweenMinutesTests {

        @Test
        @DisplayName("Should calculate minutes between LocalDateTime")
        void shouldCalculateMinutesBetween() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 13, 10, 30, 0);

            long result = DateHelper.minutesBetween(start, end);
            assertEquals(30, result);
        }
    }

    @Nested
    @DisplayName("Between Tests - Days")
    class BetweenDaysTests {

        @Test
        @DisplayName("Should calculate days between LocalDate")
        void shouldCalculateDaysBetweenLocalDate() {
            LocalDate start = LocalDate.of(2024, 2, 13);
            LocalDate end = LocalDate.of(2024, 2, 20);

            long result = DateHelper.daysBetween(start, end);
            assertEquals(7, result);
        }

        @Test
        @DisplayName("Should calculate days between LocalDateTime")
        void shouldCalculateDaysBetweenLocalDateTime() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 2, 20, 10, 0);

            long result = DateHelper.daysBetween(start, end);
            assertEquals(7, result);
        }
    }

    @Nested
    @DisplayName("Between Tests - Months")
    class BetweenMonthsTests {

        @Test
        @DisplayName("Should calculate months between LocalDate")
        void shouldCalculateMonthsBetweenLocalDate() {
            LocalDate start = LocalDate.of(2024, 2, 13);
            LocalDate end = LocalDate.of(2024, 5, 13);

            long result = DateHelper.monthsBetween(start, end);
            assertEquals(3, result);
        }

        @Test
        @DisplayName("Should calculate months between LocalDateTime")
        void shouldCalculateMonthsBetweenLocalDateTime() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0);
            LocalDateTime end = LocalDateTime.of(2024, 5, 13, 10, 0);

            long result = DateHelper.monthsBetween(start, end);
            assertEquals(3, result);
        }
    }

    @Nested
    @DisplayName("Between Tests - Years")
    class BetweenYearsTests {

        @Test
        @DisplayName("Should calculate years between LocalDate")
        void shouldCalculateYearsBetweenLocalDate() {
            LocalDate start = LocalDate.of(2024, 2, 13);
            LocalDate end = LocalDate.of(2027, 2, 13);

            long result = DateHelper.yearsBetween(start, end);
            assertEquals(3, result);
        }

        @Test
        @DisplayName("Should calculate years between LocalDateTime")
        void shouldCalculateYearsBetweenLocalDateTime() {
            LocalDateTime start = LocalDateTime.of(2024, 2, 13, 10, 0);
            LocalDateTime end = LocalDateTime.of(2027, 2, 13, 10, 0);

            long result = DateHelper.yearsBetween(start, end);
            assertEquals(3, result);
        }
    }

    @Nested
    @DisplayName("Between Tests - Deprecated Date")
    class BetweenDateTests {

        @Test
        @DisplayName("Should calculate seconds between Date (deprecated)")
        void shouldCalculateSecondsBetweenDate() throws Exception {
            Date start = SDF.parse("2024-02-13 10:00:00");
            Date end = SDF.parse("2024-02-13 10:01:00");

            @SuppressWarnings("deprecation")
            long result = DateHelper.between(start, end);
            assertEquals(60, result);
        }
    }

    // ==================== LEAP YEAR TESTS ====================

    @Nested
    @DisplayName("Leap Year Tests")
    class LeapYearTests {

        @ParameterizedTest
        @ValueSource(ints = {2000, 2004, 2008, 2012, 2016, 2020, 2024, 2028})
        @DisplayName("Should identify leap years")
        void shouldIdentifyLeapYears(int year) {
            assertTrue(DateHelper.isLeapYear(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {2001, 2002, 2003, 2005, 2006, 2007, 2009, 2010, 2011})
        @DisplayName("Should identify non-leap years")
        void shouldIdentifyNonLeapYears(int year) {
            assertFalse(DateHelper.isLeapYear(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {1900, 2100, 2200, 2300})
        @DisplayName("Should handle century years correctly")
        void shouldHandleCenturyYearsCorrectly(int year) {
            assertFalse(DateHelper.isLeapYear(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {2000, 2400})
        @DisplayName("Should handle century leap years correctly")
        void shouldHandleCenturyLeapYearsCorrectly(int year) {
            assertTrue(DateHelper.isLeapYear(year));
        }
    }

    // ==================== CONVERSION TESTS ====================

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("Should convert LocalDateTime to Date")
        void shouldConvertLocalDateTimeToDate() {
            LocalDateTime dateTime = LocalDateTime.of(2024, 2, 13, 15, 30, 45);
            Date result = DateHelper.toLegacyDate(dateTime);

            assertNotNull(result);
            LocalDateTime converted = DateHelper.toLocalDateTime(result);
            assertEquals(dateTime, converted);
        }

        @Test
        @DisplayName("Should convert LocalDate to Date")
        void shouldConvertLocalDateToDate() {
            LocalDate date = LocalDate.of(2024, 2, 13);
            Date result = DateHelper.toLegacyDate(date);

            assertNotNull(result);
            LocalDate converted = DateHelper.legacyDatetoLocalDate(result);
            assertEquals(date, converted);
        }

        @Test
        @DisplayName("Should convert Date to LocalDateTime")
        void shouldConvertDateToLocalDateTime() throws Exception {
            Date date = SDF.parse("2024-02-13 15:30:45");
            LocalDateTime result = DateHelper.toLocalDateTime(date);

            assertNotNull(result);
            assertEquals(2024, result.getYear());
            assertEquals(2, result.getMonthValue());
            assertEquals(13, result.getDayOfMonth());
        }

        @Test
        @DisplayName("Should convert Date to LocalDate")
        void shouldConvertDateToLocalDate() throws Exception {
            Date date = SDF.parse("2024-02-13 15:30:45");
            LocalDate result = DateHelper.legacyDatetoLocalDate(date);

            assertNotNull(result);
            assertEquals(LocalDate.of(2024, 2, 13), result);
        }

        @Test
        @DisplayName("Should handle null in LocalDateTime to Date conversion")
        void shouldHandleNullInLocalDateTimeToDate() {
            assertNull(DateHelper.toLegacyDate((LocalDateTime) null));
        }

        @Test
        @DisplayName("Should handle null in LocalDate to Date conversion")
        void shouldHandleNullInLocalDateToDate() {
            assertNull(DateHelper.toLegacyDate((LocalDate) null));
        }

        @Test
        @DisplayName("Should handle null in Date to LocalDateTime conversion")
        void shouldHandleNullInDateToLocalDateTime() {
            assertNull(DateHelper.toLocalDateTime(null));
        }

        @Test
        @DisplayName("Should handle null in Date to LocalDate conversion")
        void shouldHandleNullInDateToLocalDate() {
            assertNull(DateHelper.legacyDatetoLocalDate(null));
        }
    }

    // ==================== NULL HANDLING TESTS ====================

    @Nested
    @DisplayName("Null Handling Tests")
    class NullHandlingTests {

        @Test
        @DisplayName("Should throw NullPointerException for null LocalDateTime in occurredInLastXHours")
        void shouldThrowForNullInOccurredInLastXHours() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.occurredInLastXHours((LocalDateTime) null, 1));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null LocalDate in areDatesOnSameDay")
        void shouldThrowForNullInAreDatesOnSameDay() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.areDatesOnSameDay((LocalDate) null, LocalDate.now()));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null LocalDate in addDays")
        void shouldThrowForNullInAddDays() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.addDays((LocalDate) null, 1));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null LocalDate in getStartOfWeek")
        void shouldThrowForNullInGetStartOfWeek() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.getStartOfWeek((LocalDate) null));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null LocalDate in daysBetween")
        void shouldThrowForNullInDaysBetween() {
            assertThrows(NullPointerException.class,
                    () -> DateHelper.daysBetween(null, LocalDate.now()));
        }

        @Test
        @DisplayName("Should verify deprecated Date method null checks")
        void shouldVerifyDeprecatedDateNullChecks() {
            @SuppressWarnings("deprecation")
            Class<NullPointerException> npe = NullPointerException.class;

            assertThrows(npe, () -> DateHelper.occurredInLastXHours((Date) null, 1));
            assertThrows(npe, () -> DateHelper.areDatesOnSameDay((Date) null, new Date()));
            assertThrows(npe, () -> DateHelper.addDaysToDate(null, 1));
            assertThrows(npe, () -> DateHelper.getStartOfWeek((Date) null));
        }
    }

    // ==================== EDGE CASE TESTS ====================

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle leap year February 29")
        void shouldHandleLeapYearFeb29() {
            LocalDate leapDate = LocalDate.of(2024, 2, 29);
            assertTrue(DateHelper.isLeapYear(leapDate.getYear()));

            LocalDate addedMonth = DateHelper.addMonths(leapDate, 1);
            assertEquals(LocalDate.of(2024, 3, 29), addedMonth);
        }

        @Test
        @DisplayName("Should handle year boundary transitions")
        void shouldHandleYearBoundary() {
            LocalDate yearEnd = LocalDate.of(2024, 12, 31);
            LocalDate addedDay = DateHelper.addDays(yearEnd, 1);

            assertEquals(2025, addedDay.getYear());
            assertEquals(1, addedDay.getMonthValue());
            assertEquals(1, addedDay.getDayOfMonth());
        }

        @Test
        @DisplayName("Should handle month boundary transitions")
        void shouldHandleMonthBoundary() {
            LocalDate monthEnd = LocalDate.of(2024, 4, 30);
            LocalDate addedDay = DateHelper.addDays(monthEnd, 1);

            assertEquals(5, addedDay.getMonthValue());
            assertEquals(1, addedDay.getDayOfMonth());
        }

        @Test
        @DisplayName("Should handle zero and negative day/month/year additions")
        void shouldHandleZeroAndNegativeAdditions() {
            LocalDate date = LocalDate.of(2024, 6, 15);

            assertEquals(date, DateHelper.addDays(date, 0));
            assertEquals(date, DateHelper.addMonths(date, 0));
            assertEquals(date, DateHelper.addYears(date, 0));

            LocalDate prevYear = DateHelper.addYears(date, -1);
            assertEquals(2023, prevYear.getYear());
        }

        @Test
        @DisplayName("Should handle far future dates")
        void shouldHandleFarFutureDates() {
            LocalDate farFuture = LocalDate.of(2999, 12, 31);
            assertTrue(DateHelper.isFuture(farFuture));

            LocalDate thirtyYearsLater = DateHelper.addYears(farFuture, 30);
            assertEquals(3029, thirtyYearsLater.getYear());
        }

        @Test
        @DisplayName("Should handle historical dates")
        void shouldHandleHistoricalDates() {
            LocalDate historical = LocalDate.of(1900, 1, 1);
            assertTrue(DateHelper.isPast(historical));

            LocalDate addedDays = DateHelper.addDays(historical, 100);
            assertEquals(LocalDate.of(1900, 4, 11), addedDays);
        }
    }
}