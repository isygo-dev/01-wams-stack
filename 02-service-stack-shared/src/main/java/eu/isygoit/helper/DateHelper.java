package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility class for performing common date-related operations.
 * Provides methods to manipulate, compare, and format dates efficiently.
 */
public interface DateHelper {

    Logger logger = LoggerFactory.getLogger(DateHelper.class);

    // Constants for defining the valid calendar range
    LocalDateTime CALENDAR_START = LocalDateTime.of(1900, 1, 1, 0, 0);
    LocalDateTime CALENDAR_END = LocalDateTime.of(2999, 12, 31, 23, 59);

    // List of date formats to attempt when parsing date strings
    List<String> DATE_PATTERNS = List.of(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", // ISO 8601 with millisecond precision
            "yyyy-MM-dd'T'HH:mm:ssXXX",     // ISO 8601 with time zone offset
            "yyyy-MM-dd'T'HH:mm",           // ISO 8601 without time zone
            "yyyy-MM-dd",                   // ISO 8601 date format
            "yyyy/MM/dd",                   // Date with slashes as separators
            "dd-MM-yyyy'T'HH:mm:ss.SSSXXX", // Custom format with day first
            "dd-MM-yyyy'T'HH:mm:ssXXX",     // Custom format with day first
            "dd-MM-yyyy'T'HH:mm",           // Custom format with day first
            "dd-MM-yyyy",                   // Day first with hyphen separator
            "dd/MM/yyyy",                   // Day first with slashes separator
            "dd/MM/yyyy HH:mm",             // Day first with time (24-hour format)
            "yyyyMMdd",                     // Compact date format with no delimiters
            "ddMMyyyy",                     // Compact day-first format with no delimiters
            "yyyyMMddHHmm",                 // Compact date-time format
            "yyyyMMddHHmmss",               // Full date-time format
            "MM-dd-yyyy",                   // US format with month first
            "MM/dd/yyyy",                   // US format with slashes
            "MM/dd/yyyy HH:mm",             // US format with time
            "EEEE, dd MMM yyyy HH:mm:ss z", // RFC 1123 date format (e.g., Wed, 02 Feb 2025 15:00:00 GMT)
            "MMMM dd, yyyy",                // Full month name (e.g. February 12, 2025)
            "yyyy/MM/dd HH:mm:ss",          // Date-time with slashes
            "epoch",                         // Unix epoch timestamp
            "epoch_second"                   // Unix epoch timestamp in seconds
    );

    // --- Date Transformation Methods ---

    /**
     * Converts an absolute date string to a Date object using known patterns.
     * If parsing fails, the method returns a default date or throws an exception.
     * Logs each parsing attempt and provides useful debugging information.
     *
     * @param dateString    the date string to parse
     * @param defaultIfNull the default value to return if parsing fails
     * @return the parsed Date object or the default value if parsing fails
     */
    public static Date parseDateString(String dateString, Date defaultIfNull) {
        logger.debug("Attempting to parse date string: {}", dateString);

        if (!StringUtils.hasText(dateString) || "null".equalsIgnoreCase(dateString)) {
            logger.warn("Input date string is empty or null, returning default date.");
            return defaultIfNull;
        }

        // Try each pattern and log the process
        return DATE_PATTERNS.stream()
                .map(pattern -> {
                    try {
                        logger.debug("Trying to parse with pattern: {}", pattern);
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                        LocalDate localDate = LocalDate.parse(dateString, formatter);

                        // Manually check if the parsed date matches the input date to prevent auto-correction
                        if (!dateString.equals(formatter.format(localDate))) {
                            throw new DateTimeParseException("Invalid date", dateString, 0);
                        }

                        // Convert LocalDate to java.util.Date
                        return java.util.Date.from(localDate.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant());
                    } catch (DateTimeParseException e) {
                        logger.debug("Failed to parse date with pattern {}: {}", pattern, e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = "Invalid date format for input string: " + dateString;
                    logger.error(errorMessage);
                    return new IllegalArgumentException(errorMessage);
                });
    }

    /**
     * Converts a Date to an ISO 8601 formatted date string.
     * Logs the conversion process and the formatted result.
     *
     * @param date the date to convert
     * @return the ISO 8601 formatted date string
     */
    public static String formatDateToIsoString(Date date) {
        logger.debug("Attempting to format date: {}", date);

        return Optional.ofNullable(date)
                .map(d -> {
                    String formattedDate = d.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    logger.debug("Successfully formatted date {} to ISO string: {}", date, formattedDate);
                    return formattedDate;
                })
                .orElseGet(() -> {
                    logger.warn("Provided date is null, returning null.");
                    return null;
                });
    }

    /**
     * Determines if the given date occurred within the last specified number of hours.
     * Logs the check, including the calculated threshold and the result.
     *
     * @param date  the date to check
     * @param hours the number of hours to check against
     * @return true if the date is within the last specified hours, false otherwise
     */
    public static boolean occurredInLastXHours(Date date, int hours) {
        Objects.requireNonNull(date, "Date must not be null");

        Instant threshold = Instant.now().minus(hours, ChronoUnit.HOURS);
        boolean isRecent = date.toInstant().isAfter(threshold);

        logger.debug("Checking if date {} occurred in the last {} hours. Threshold: {}, Result: {}", date, hours, threshold, isRecent);
        return isRecent;
    }

    /**
     * Checks if two dates fall on the same calendar day.
     * Logs the comparison result.
     *
     * @param date1 the first date
     * @param date2 the second date
     * @return true if both dates are on the same day, false otherwise
     */
    public static boolean areDatesOnSameDay(Date date1, Date date2) {
        Objects.requireNonNull(date1, "First date must not be null");
        Objects.requireNonNull(date2, "Second date must not be null");

        boolean isSameDay = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .isEqual(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        logger.debug("Comparing dates {} and {}: Are they on the same day? {}", date1, date2, isSameDay);
        return isSameDay;
    }

    /**
     * Checks if the given date is today's date.
     * Logs the comparison and result.
     *
     * @param date the date to check
     * @return true if the date is today, false otherwise
     */
    public static boolean isToday(Date date) {
        boolean isToday = areDatesOnSameDay(date, new Date());
        logger.debug("Checking if date {} is today. Result: {}", date, isToday);
        return isToday;
    }

    /**
     * Adds or subtracts a specific number of days to/from a given date.
     * Logs the calculation process.
     *
     * @param date the original date
     * @param days the number of days to add (can be negative to subtract)
     * @return the new date with days added/subtracted
     */
    public static Date addDaysToDate(Date date, int days) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate newDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(days);

        Date result = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        logger.debug("Adding {} days to {} results in new date: {}", days, date, result);
        return result;
    }

    /**
     * Adds or subtracts a specific number of months to/from a given date.
     * Logs the calculation process.
     *
     * @param date   the original date
     * @param months the number of months to add (can be negative to subtract)
     * @return the new date with months added/subtracted
     */
    public static Date addMonthsToDate(Date date, int months) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate newDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusMonths(months);

        Date result = Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        logger.debug("Adding {} months to {} results in new date: {}", months, date, result);
        return result;
    }

    /**
     * Checks if a given year is a leap year.
     * Logs the check and result.
     *
     * @param year the year to check
     * @return true if it's a leap year, false otherwise
     */
    public static boolean isLeapYear(int year) {
        boolean leapYear = Year.isLeap(year);
        logger.debug("Is the year {} a leap year? {}", year, leapYear);
        return leapYear;
    }

    /**
     * Returns the start of the week for a given date (00:00:00 on Monday).
     * Logs the transformation and result.
     *
     * @param date the date to find the start of the week
     * @return the start of the week (Monday at 00:00:00)
     */
    public static Date getStartOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate startOfWeek = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .with(DayOfWeek.MONDAY);

        Date result = Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        logger.debug("Start of the week for {} is: {}", date, result);
        return result;
    }

    /**
     * Returns the end of the week for a given date (23:59:59 on Sunday).
     * Logs the transformation and result.
     *
     * @param date the date to find the end of the week
     * @return the end of the week (Sunday at 23:59:59)
     */
    public static Date getEndOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate endOfWeek = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .with(DayOfWeek.SUNDAY);

        Date result = Date.from(endOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
        logger.debug("End of the week for {} is: {}", date, result);
        return result;
    }

    /**
     * Converts a Date to a human-readable format (e.g., "February 12, 2025").
     * Logs the conversion process.
     *
     * @param date the date to convert
     * @return the formatted string
     */
    public static String formatDateToHumanReadable(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        String formatted = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
                .format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        logger.debug("Formatted date {} to human-readable format: {}", date, formatted);
        return formatted;
    }
}