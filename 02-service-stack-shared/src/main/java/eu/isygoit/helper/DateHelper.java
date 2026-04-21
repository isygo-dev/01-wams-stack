package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * The interface Date helper with support for modern Java 17 date/time types.
 * All methods using java.util.Date are deprecated in favor of LocalDate, LocalDateTime, and LocalTime.
 */
public interface DateHelper {

    /**
     * The constant logger.
     */
    Logger logger = LoggerFactory.getLogger(DateHelper.class);

    /**
     * The constant CALENDAR_START.
     */
    LocalDateTime CALENDAR_START = LocalDateTime.of(1900, 1, 1, 0, 0);
    /**
     * The constant CALENDAR_END.
     */
    LocalDateTime CALENDAR_END = LocalDateTime.of(2999, 12, 31, 23, 59);

    /**
     * The constant TIME_START.
     */
    LocalTime TIME_START = LocalTime.of(0, 0, 0, 0);
    /**
     * The constant TIME_END.
     */
    LocalTime TIME_END = LocalTime.of(23, 59, 59, 999999999);

    /**
     * The constant DATE_PATTERNS.
     */
    List<String> DATE_PATTERNS = List.of(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd-MM-yyyy'T'HH:mm:ss.SSSXXX",
            "dd-MM-yyyy'T'HH:mm:ssXXX",
            "dd-MM-yyyy'T'HH:mm",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "dd/MM/yyyy HH:mm",
            "yyyyMMdd",
            "ddMMyyyy",
            "yyyyMMddHHmm",
            "yyyyMMddHHmmss",
            "MM-dd-yyyy",
            "MM/dd/yyyy",
            "MM/dd/yyyy HH:mm",
            "EEEE, d MMM yyyy HH:mm:ss z",
            "EEEE, dd MMM yyyy HH:mm:ss z",
            "EEE, d MMM yyyy HH:mm:ss z",
            "EEE, dd MMM yyyy HH:mm:ss z",
            "EEEE, dd MMM yyyy HH:mm:ss 'GMT'",
            "EEE, dd MMM yyyy HH:mm:ss 'GMT'",
            "E, dd MMM yyyy HH:mm:ss z",
            "E, d MMM yyyy HH:mm:ss z",
            "MMMM d, yyyy",
            "MMMM dd, yyyy",
            "yyyy/MM/dd HH:mm:ss",
            "EEE MMM dd HH:mm:ss z yyyy",
            "epoch",
            "epoch_second"
    );

    List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
    );

    // ==================== NEW METHODS - LocalDateTime ====================

    /**
     * Parse date string to LocalDateTime.
     *
     * @param dateString    the date string
     * @param defaultIfNull the default if null
     * @return the LocalDateTime
     */
    static LocalDateTime parseLocalDateTime(String dateString, LocalDateTime defaultIfNull) {
        logger.debug("Attempting to parse LocalDateTime from string: {}", dateString);

        if (!StringUtils.hasText(dateString) || "null".equalsIgnoreCase(dateString)) {
            logger.warn("Input date string is empty or null, returning default LocalDateTime.");
            return defaultIfNull;
        }

        return DATE_PATTERNS.stream()
                .map(pattern -> tryParseLocalDateTime(dateString, pattern))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = "Invalid date format for input string: " + dateString;
                    logger.error(errorMessage);
                    return new IllegalArgumentException(errorMessage);
                });
    }

    private static LocalDateTime tryParseLocalDateTime(String dateString, String pattern) {
        try {
            if ("epoch".equalsIgnoreCase(pattern)) {
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dateString)), ZoneId.systemDefault());
            } else if ("epoch_second".equalsIgnoreCase(pattern)) {
                return LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(dateString)), ZoneId.systemDefault());
            }

            var formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            var temporalAccessor = formatter.parse(dateString);

            LocalDateTime result;
            try {
                result = LocalDateTime.from(temporalAccessor);
            } catch (Exception e1) {
                try {
                    result = ZonedDateTime.from(temporalAccessor).toLocalDateTime();
                } catch (Exception e2) {
                    try {
                        result = OffsetDateTime.from(temporalAccessor).toLocalDateTime();
                    } catch (Exception e3) {
                        try {
                            result = LocalDate.from(temporalAccessor).atStartOfDay();
                        } catch (Exception e4) {
                            return null;
                        }
                    }
                }
            }

            if (result != null) {
                // Validation for strict mode
                try {
                    ZoneId zoneId = ZoneId.systemDefault();
                    boolean skipStrict = false;
                    if (pattern.contains("z") || pattern.contains("VV") || pattern.contains("X") || pattern.contains("x")) {
                        try {
                            zoneId = ZoneId.from(temporalAccessor);
                        } catch (Exception e) {
                            zoneId = ZoneId.of("UTC");
                        }
                        skipStrict = true;
                    }

                    if (!skipStrict) {
                        String reformatted = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                                .format(result.atZone(zoneId));

                        if (!dateString.equalsIgnoreCase(reformatted)) {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            return result;
        } catch (Exception e) {
            logger.debug("Failed to parse LocalDateTime with pattern {}: {}", pattern, e.getMessage());
            return null;
        }
    }

    /**
     * Parse date string to LocalDate.
     *
     * @param dateString    the date string
     * @param defaultIfNull the default if null
     * @return the LocalDate
     */
    static LocalDate parseLocalDate(String dateString, LocalDate defaultIfNull) {
        logger.debug("Attempting to parse LocalDate from string: {}", dateString);

        if (!StringUtils.hasText(dateString) || "null".equalsIgnoreCase(dateString)) {
            logger.warn("Input date string is empty or null, returning default LocalDate.");
            return defaultIfNull;
        }

        LocalDateTime dateTime = parseLocalDateTime(dateString, null);
        return dateTime != null ? dateTime.toLocalDate() : defaultIfNull;
    }

    /**
     * Format LocalDateTime to ISO string.
     *
     * @param dateTime the LocalDateTime
     * @return the ISO formatted string
     */
    static String formatToIsoString(LocalDateTime dateTime) {
        logger.debug("Attempting to format LocalDateTime: {}", dateTime);

        return Optional.ofNullable(dateTime)
                .map(dt -> dt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElseGet(() -> {
                    logger.warn("Provided LocalDateTime is null, returning null.");
                    return null;
                });
    }

    /**
     * Format LocalDate to ISO string.
     *
     * @param date the LocalDate
     * @return the ISO formatted string
     */
    static String formatToIsoString(LocalDate date) {
        logger.debug("Attempting to format LocalDate: {}", date);

        return Optional.ofNullable(date)
                .map(d -> d.format(DateTimeFormatter.ISO_DATE))
                .orElseGet(() -> {
                    logger.warn("Provided LocalDate is null, returning null.");
                    return null;
                });
    }

    /**
     * Format LocalDateTime to human readable string.
     *
     * @param dateTime the LocalDateTime
     * @return the formatted string
     */
    static String formatToHumanReadable(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");

        return DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss", Locale.ENGLISH)
                .format(dateTime);
    }

    /**
     * Format LocalDate to human readable string.
     *
     * @param date the LocalDate
     * @return the formatted string
     */
    static String formatToHumanReadable(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");

        return DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
                .format(date);
    }

    /**
     * Check if LocalDateTime occurred in last X hours.
     *
     * @param dateTime the LocalDateTime
     * @param hours    the hours
     * @return true if within last X hours
     */
    static boolean occurredInLastXHours(LocalDateTime dateTime, int hours) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");

        LocalDateTime threshold = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        boolean isRecent = dateTime.isAfter(threshold);

        logger.debug("Checking if LocalDateTime {} occurred in the last {} hours. Threshold: {}, Result: {}", dateTime, hours, threshold, isRecent);
        return isRecent;
    }

    /**
     * Check if two LocalDate are on the same day.
     *
     * @param date1 the first LocalDate
     * @param date2 the second LocalDate
     * @return true if on same day
     */
    static boolean areDatesOnSameDay(LocalDate date1, LocalDate date2) {
        Objects.requireNonNull(date1, "First LocalDate must not be null");
        Objects.requireNonNull(date2, "Second LocalDate must not be null");

        return date1.isEqual(date2);
    }

    /**
     * Check if two LocalDateTime are on the same day.
     *
     * @param dateTime1 the first LocalDateTime
     * @param dateTime2 the second LocalDateTime
     * @return true if on same day
     */
    static boolean areDatesOnSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        Objects.requireNonNull(dateTime1, "First LocalDateTime must not be null");
        Objects.requireNonNull(dateTime2, "Second LocalDateTime must not be null");

        return dateTime1.toLocalDate().isEqual(dateTime2.toLocalDate());
    }

    /**
     * Add days to LocalDate.
     *
     * @param date the LocalDate
     * @param days the days to add
     * @return the new LocalDate
     */
    static LocalDate addDays(LocalDate date, int days) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.plusDays(days);
    }

    /**
     * Add days to LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @param days     the days to add
     * @return the new LocalDateTime
     */
    static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.plusDays(days);
    }

    /**
     * Add months to LocalDate.
     *
     * @param date   the LocalDate
     * @param months the months to add
     * @return the new LocalDate
     */
    static LocalDate addMonths(LocalDate date, int months) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.plusMonths(months);
    }

    /**
     * Add months to LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @param months   the months to add
     * @return the new LocalDateTime
     */
    static LocalDateTime addMonths(LocalDateTime dateTime, int months) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.plusMonths(months);
    }

    /**
     * Add years to LocalDate.
     *
     * @param date  the LocalDate
     * @param years the years to add
     * @return the new LocalDate
     */
    static LocalDate addYears(LocalDate date, int years) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.plusYears(years);
    }

    /**
     * Add years to LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @param years    the years to add
     * @return the new LocalDateTime
     */
    static LocalDateTime addYears(LocalDateTime dateTime, int years) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.plusYears(years);
    }

    /**
     * Check if year is a leap year.
     *
     * @param year the year
     * @return true if leap year
     */
    static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    /**
     * Get start of week (Monday) for LocalDate.
     *
     * @param date the LocalDate
     * @return the start of week
     */
    static LocalDate getStartOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * Get start of week (Monday) for LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @return the start of week
     */
    static LocalDateTime getStartOfWeek(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Get end of week (Sunday) for LocalDate.
     *
     * @param date the LocalDate
     * @return the end of week
     */
    static LocalDate getEndOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.with(DayOfWeek.SUNDAY);
    }

    /**
     * Get end of week (Sunday) for LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @return the end of week
     */
    static LocalDateTime getEndOfWeek(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Get start of month for LocalDate.
     *
     * @param date the LocalDate
     * @return the start of month
     */
    static LocalDate getStartOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.withDayOfMonth(1);
    }

    /**
     * Get start of month for LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @return the start of month
     */
    static LocalDateTime getStartOfMonth(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Get end of month for LocalDate.
     *
     * @param date the LocalDate
     * @return the end of month
     */
    static LocalDate getEndOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Get end of month for LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @return the end of month
     */
    static LocalDateTime getEndOfMonth(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        int lastDay = dateTime.toLocalDate().lengthOfMonth();
        return dateTime.withDayOfMonth(lastDay).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Check if LocalDate is today.
     *
     * @param date the LocalDate
     * @return true if today
     */
    static boolean isToday(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.isEqual(LocalDate.now());
    }

    /**
     * Check if LocalDateTime is today.
     *
     * @param dateTime the LocalDateTime
     * @return true if today
     */
    static boolean isToday(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.toLocalDate().isEqual(LocalDate.now());
    }

    /**
     * Check if LocalDate is in the past.
     *
     * @param date the LocalDate
     * @return true if in the past
     */
    static boolean isPast(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.isBefore(LocalDate.now());
    }

    /**
     * Check if LocalDateTime is in the past.
     *
     * @param dateTime the LocalDateTime
     * @return true if in the past
     */
    static boolean isPast(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Check if LocalDate is in the future.
     *
     * @param date the LocalDate
     * @return true if in the future
     */
    static boolean isFuture(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.isAfter(LocalDate.now());
    }

    /**
     * Check if LocalDateTime is in the future.
     *
     * @param dateTime the LocalDateTime
     * @return true if in the future
     */
    static boolean isFuture(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Get maximum of two LocalDate values.
     *
     * @param date1 the first LocalDate
     * @param date2 the second LocalDate
     * @return the maximum LocalDate
     */
    static LocalDate max(LocalDate date1, LocalDate date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.isAfter(date2) ? date1 : date2;
    }

    /**
     * Get maximum of two LocalDateTime values.
     *
     * @param dateTime1 the first LocalDateTime
     * @param dateTime2 the second LocalDateTime
     * @return the maximum LocalDateTime
     */
    static LocalDateTime max(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null) return dateTime2;
        if (dateTime2 == null) return dateTime1;
        return dateTime1.isAfter(dateTime2) ? dateTime1 : dateTime2;
    }

    /**
     * Get minimum of two LocalDate values.
     *
     * @param date1 the first LocalDate
     * @param date2 the second LocalDate
     * @return the minimum LocalDate
     */
    static LocalDate min(LocalDate date1, LocalDate date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.isBefore(date2) ? date1 : date2;
    }

    /**
     * Get minimum of two LocalDateTime values.
     *
     * @param dateTime1 the first LocalDateTime
     * @param dateTime2 the second LocalDateTime
     * @return the minimum LocalDateTime
     */
    static LocalDateTime min(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null) return dateTime2;
        if (dateTime2 == null) return dateTime1;
        return dateTime1.isBefore(dateTime2) ? dateTime1 : dateTime2;
    }

    /**
     * Calculate seconds between two LocalDateTime values.
     *
     * @param start the start LocalDateTime
     * @param end   the end LocalDateTime
     * @return the number of seconds
     */
    static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Calculate hours between two LocalDateTime values.
     *
     * @param start the start LocalDateTime
     * @param end   the end LocalDateTime
     * @return the number of hours
     */
    static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculate minutes between two LocalDateTime values.
     *
     * @param start the start LocalDateTime
     * @param end   the end LocalDateTime
     * @return the number of minutes
     */
    static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Calculate days between two LocalDate values.
     *
     * @param start the start LocalDate
     * @param end   the end LocalDate
     * @return the number of days
     */
    static long daysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start LocalDate must not be null");
        Objects.requireNonNull(end, "End LocalDate must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate days between two LocalDateTime values.
     *
     * @param start the start LocalDateTime
     * @param end   the end LocalDateTime
     * @return the number of days
     */
    static long daysBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate months between two LocalDate values.
     *
     * @param start the start LocalDate
     * @param end   the end LocalDate
     * @return the number of months
     */
    static long monthsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start LocalDate must not be null");
        Objects.requireNonNull(end, "End LocalDate must not be null");
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calculate months between two LocalDateTime values.
     *
     * @param start the start LocalDateTime
     * @param end   the end LocalDateTime
     * @return the number of months
     */
    static long monthsBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calculate years between two LocalDate values.
     *
     * @param start the start LocalDate
     * @param end   the end LocalDate
     * @return the number of years
     */
    static long yearsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start LocalDate must not be null");
        Objects.requireNonNull(end, "End LocalDate must not be null");
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Calculate years between two LocalDateTime values.
     *
     * @param start the start LocalDateTime
     * @param end   the end LocalDateTime
     * @return the number of years
     */
    static long yearsBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Convert LocalDateTime to Date.
     *
     * @param dateTime the LocalDateTime
     * @return the Date
     */
    static Date localDateTimetoLegacyDate(LocalDateTime dateTime) {
        if (dateTime != null) {
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Convert LocalDate to Date (at start of day).
     *
     * @param date the LocalDate
     * @return the Date
     */
    static Date localDatetoLegacyDate(LocalDate date) {
        if (date != null) {
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Convert Date to LocalDateTime.
     *
     * @param date the Date to convert
     * @return the LocalDateTime
     */
    static LocalDateTime legacyDatetoLocalDateTime(Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }

    /**
     * Convert Date to LocalDate.
     *
     * @param date the Date to convert
     * @return the LocalDate
     */
    static LocalDate legacyDatetoLocalDate(Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }

    // ==================== DEPRECATED METHODS - java.util.Date ====================

    /**
     * @deprecated Use {@link #parseLocalDateTime(String, LocalDateTime)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date parseDateString(String dateString, Date defaultIfNull) {
        logger.debug("Attempting to parse date string: {}", dateString);

        if (!StringUtils.hasText(dateString) || "null".equalsIgnoreCase(dateString)) {
            logger.warn("Input date string is empty or null, returning default date.");
            return defaultIfNull;
        }

        return DATE_PATTERNS.stream()
                .map(pattern -> tryParseDate(dateString, pattern))
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = "Invalid date format for input string: " + dateString;
                    logger.error(errorMessage);
                    return new IllegalArgumentException(errorMessage);
                });
    }

    private static Date tryParseDate(String dateString, String pattern) {
        try {
            if ("epoch".equalsIgnoreCase(pattern)) {
                return new Date(Long.parseLong(dateString));
            } else if ("epoch_second".equalsIgnoreCase(pattern)) {
                return new Date(Long.parseLong(dateString) * 1000);
            }

            var formatter = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH);
            var temporalAccessor = formatter.parse(dateString);

            Date result;
            try {
                result = Date.from(ZonedDateTime.from(temporalAccessor).toInstant());
            } catch (Exception e1) {
                try {
                    result = Date.from(OffsetDateTime.from(temporalAccessor).toInstant());
                } catch (Exception e2) {
                    try {
                        result = Date.from(LocalDateTime.from(temporalAccessor).atZone(ZoneId.systemDefault()).toInstant());
                    } catch (Exception e3) {
                        try {
                            result = Date.from(LocalDate.from(temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant());
                        } catch (Exception e4) {
                            return null;
                        }
                    }
                }
            }

            if (result != null) {
                try {
                    ZoneId zoneId = ZoneId.systemDefault();
                    boolean skipStrict = false;
                    if (pattern.contains("z") || pattern.contains("VV") || pattern.contains("X") || pattern.contains("x")) {
                        try {
                            zoneId = ZoneId.from(temporalAccessor);
                        } catch (Exception e) {
                            zoneId = ZoneId.of("UTC");
                        }
                        skipStrict = true;
                    }

                    if (!skipStrict) {
                        String reformatted = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                                .format(result.toInstant().atZone(zoneId));

                        if (!dateString.equalsIgnoreCase(reformatted)) {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            return result;
        } catch (Exception e) {
            logger.debug("Failed to parse date with pattern {}: {}", pattern, e.getMessage());
            return null;
        }
    }

    /**
     * @deprecated Use {@link #formatToIsoString(LocalDateTime)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static String formatDateToIsoString(Date date) {
        logger.debug("Attempting to format date: {}", date);

        return Optional.ofNullable(date)
                .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElseGet(() -> {
                    logger.warn("Provided date is null, returning null.");
                    return null;
                });
    }

    /**
     * @deprecated Use {@link #occurredInLastXHours(LocalDateTime, int)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean occurredInLastXHours(Date date, int hours) {
        Objects.requireNonNull(date, "Date must not be null");

        Instant threshold = Instant.now().minus(hours, ChronoUnit.HOURS);
        boolean isRecent = date.toInstant().isAfter(threshold);

        logger.debug("Checking if date {} occurred in the last {} hours. Threshold: {}, Result: {}", date, hours, threshold, isRecent);
        return isRecent;
    }

    /**
     * @deprecated Use {@link #areDatesOnSameDay(LocalDate, LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean areDatesOnSameDay(Date date1, Date date2) {
        Objects.requireNonNull(date1, "First date must not be null");
        Objects.requireNonNull(date2, "Second date must not be null");

        return date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .isEqual(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * @deprecated Use {@link #addDays(LocalDate, int)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date addDaysToDate(Date date, int days) {
        Objects.requireNonNull(date, "Date must not be null");

        var newDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(days);

        return Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @deprecated Use {@link #addMonths(LocalDate, int)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date addMonthsToDate(Date date, int months) {
        Objects.requireNonNull(date, "Date must not be null");

        var newDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusMonths(months);

        return Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @deprecated Use {@link #getStartOfWeek(LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date getStartOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        var startOfWeek = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .with(DayOfWeek.MONDAY);

        return Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @deprecated Use {@link #getEndOfWeek(LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date getEndOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        var endOfWeek = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .with(DayOfWeek.SUNDAY);

        return Date.from(endOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @deprecated Use {@link #formatToHumanReadable(LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static String formatDateToHumanReadable(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        return DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
                .format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * @deprecated Use {@link #isToday(LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean isToday(Date date) {
        return areDatesOnSameDay(date, new Date());
    }

    /**
     * @deprecated Use {@link #clearTime(LocalDateTime)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date clearTime(Date date) {
        if (date == null) return null;

        var calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * Clear time from LocalDateTime.
     *
     * @param dateTime the LocalDateTime
     * @return the LocalDateTime with time set to 00:00:00
     */
    static LocalDateTime clearTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Clear time from LocalDate (returns the same LocalDate since LocalDate has no time component).
     *
     * @param date the LocalDate
     * @return the same LocalDate
     */
    static LocalDate clearTime(LocalDate date) {
        if (date == null) return null;
        return date; // LocalDate has no time component
    }

    /**
     * @deprecated Use {@link #hasTime(LocalDateTime)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean hasTime(Date date) {
        if (date == null) return false;

        var calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.HOUR_OF_DAY) > 0 || calendar.get(Calendar.MINUTE) > 0 ||
                calendar.get(Calendar.SECOND) > 0 || calendar.get(Calendar.MILLISECOND) > 0;
    }

    /**
     * Check if LocalDateTime has time component (not 00:00:00).
     *
     * @param dateTime the LocalDateTime
     * @return true if has time
     */
    static boolean hasTime(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        return dateTime.getHour() > 0 || dateTime.getMinute() > 0 ||
                dateTime.getSecond() > 0 || dateTime.getNano() > 0;
    }

    /**
     * Check if LocalDate has time component.
     * LocalDate never has a time component, so this always returns false.
     *
     * @param date the LocalDate
     * @return always false since LocalDate has no time
     */
    static boolean hasTime(LocalDate date) {
        return false; // LocalDate has no time component
    }

    /**
     * @deprecated Use {@link #max(LocalDate, LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date max(Date d1, Date d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.after(d2) ? d1 : d2;
    }

    /**
     * @deprecated Use {@link #min(LocalDate, LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date min(Date d1, Date d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.before(d2) ? d1 : d2;
    }

    /**
     * @deprecated Use {@link #secondsBetween(LocalDateTime, LocalDateTime)},
     * {@link #daysBetween(LocalDate, LocalDate)},
     * {@link #monthsBetween(LocalDate, LocalDate)}, or
     * {@link #yearsBetween(LocalDate, LocalDate)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static long between(Date lowDate, Date highDate) {
        return ChronoUnit.SECONDS.between(lowDate.toInstant(), highDate.toInstant());
    }

    /**
     * @deprecated Use {@link #toLocalDateTime(Date)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date toLegacyDate(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @deprecated Use {@link #toLocalDate(Date)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date toLegacyDate(LocalDate localDate) {
        if (localDate != null) {
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @deprecated Use {@link #toLocalDateTime(Date)} instead
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static java.time.LocalDateTime toLocalDateTime(Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }
}