package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Utility interface providing comprehensive, thread-safe date and time handling
 * for both legacy {@link java.util.Date} and modern Java Time API types
 * ({@link LocalDate}, {@link LocalDateTime}, {@link LocalTime}, etc.).
 * <p>
 * All new/public methods are based on the modern Java Time API (Java 8+)
 * and should be preferred for new code. Legacy {@code java.util.Date} methods
 * are retained for backward compatibility and are marked {@code @Deprecated}
 * (for removal in a future version). They delegate to the modern implementations
 * wherever possible to avoid code duplication while preserving exact original behavior.
 * </p>
 * <p>
 * Supports parsing of a wide range of common date/time string formats (including ISO,
 * RFC, epoch, and localized variants) with robust fallback and strict round-trip validation.
 * Provides rich utilities for date arithmetic, comparisons, range checks, formatting,
 * and calendar operations.
 * </p>
 *
 * @since 2.0
 */
public interface DateHelper {

    /**
     * The logger for this utility.
     */
    Logger logger = LoggerFactory.getLogger(DateHelper.class);

    /**
     * The constant representing the earliest supported calendar date/time.
     */
    LocalDateTime CALENDAR_START = LocalDateTime.of(1900, 1, 1, 0, 0);

    /**
     * The constant representing the latest supported calendar date/time.
     */
    LocalDateTime CALENDAR_END = LocalDateTime.of(2999, 12, 31, 23, 59);

    /**
     * The constant representing the start of a day (00:00:00.000000000).
     */
    LocalTime TIME_START = LocalTime.of(0, 0, 0, 0);

    /**
     * The constant representing the end of a day (23:59:59.999999999).
     */
    LocalTime TIME_END = LocalTime.of(23, 59, 59, 999999999);

    /**
     * Supported date/time patterns for parsing (in priority order).
     * <p>
     * Includes ISO variants, common European/US formats, epoch timestamps,
     * and several RFC-style patterns with timezone information.
     * </p>
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

    // ==================== MODERN METHODS - Java Time API ====================

    /**
     * Parses the given date string into a {@link LocalDateTime} using the supported patterns.
     * <p>
     * If the input is blank or equals "null" (case-insensitive), the {@code defaultIfNull}
     * value is returned immediately. Otherwise, patterns are tried in order until a
     * successful parse is found. Strict round-trip validation is performed for non-zoned
     * patterns to ensure the entire input was consumed correctly.
     * </p>
     *
     * @param dateString    the date/time string to parse (may be {@code null} or blank)
     * @param defaultIfNull the value to return if {@code dateString} is blank or {@code null}
     * @return the parsed {@link LocalDateTime} or {@code defaultIfNull}
     * @throws IllegalArgumentException if {@code dateString} is non-blank but cannot be parsed
     *                                  with any supported pattern
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
                // Strict round-trip validation (skipped for patterns containing zone/offset info)
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
     * Parses the given date string into a {@link LocalDate} (time component is ignored/truncated).
     * Delegates to {@link #parseLocalDateTime(String, LocalDateTime)} internally.
     *
     * @param dateString    the date string to parse (may be {@code null} or blank)
     * @param defaultIfNull the value to return if {@code dateString} is blank or {@code null}
     * @return the parsed {@link LocalDate} or {@code defaultIfNull}
     * @throws IllegalArgumentException if {@code dateString} is non-blank but cannot be parsed
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
     * Formats the given {@link LocalDateTime} to an ISO-8601 offset date-time string
     * (e.g. {@code 2023-01-01T12:00:00+01:00}).
     *
     * @param dateTime the {@link LocalDateTime} to format
     * @return the ISO-formatted string, or {@code null} if the input is {@code null}
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
     * Formats the given {@link LocalDate} to an ISO-8601 date string (e.g. {@code 2023-01-01}).
     *
     * @param date the {@link LocalDate} to format
     * @return the ISO-formatted string, or {@code null} if the input is {@code null}
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
     * Formats the given {@link LocalDateTime} to a human-readable English string
     * (e.g. {@code January 01, 2023 12:00:00}).
     *
     * @param dateTime the {@link LocalDateTime} to format (must not be {@code null})
     * @return the formatted human-readable string
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static String formatToHumanReadable(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");

        return DateTimeFormatter.ofPattern("MMMM dd, yyyy HH:mm:ss", Locale.ENGLISH)
                .format(dateTime);
    }

    /**
     * Formats the given {@link LocalDate} to a human-readable English string
     * (e.g. {@code January 01, 2023}).
     *
     * @param date the {@link LocalDate} to format (must not be {@code null})
     * @return the formatted human-readable string
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static String formatToHumanReadable(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");

        return DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
                .format(date);
    }

    /**
     * Checks whether the given {@link LocalDateTime} occurred within the last {@code hours} hours.
     *
     * @param dateTime the {@link LocalDateTime} to check (must not be {@code null})
     * @param hours    the number of hours (may be negative)
     * @return {@code true} if {@code dateTime} is after {@code LocalDateTime.now() - hours}
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static boolean occurredInLastXHours(LocalDateTime dateTime, int hours) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");

        LocalDateTime threshold = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
        boolean isRecent = dateTime.isAfter(threshold);

        logger.debug("Checking if LocalDateTime {} occurred in the last {} hours. Threshold: {}, Result: {}", dateTime, hours, threshold, isRecent);
        return isRecent;
    }

    /**
     * Determines whether two {@link LocalDate} instances represent the same calendar day.
     *
     * @param date1 the first {@link LocalDate} (must not be {@code null})
     * @param date2 the second {@link LocalDate} (must not be {@code null})
     * @return {@code true} if both dates are equal
     * @throws NullPointerException if either argument is {@code null}
     */
    static boolean areDatesOnSameDay(LocalDate date1, LocalDate date2) {
        Objects.requireNonNull(date1, "First LocalDate must not be null");
        Objects.requireNonNull(date2, "Second LocalDate must not be null");

        return date1.isEqual(date2);
    }

    /**
     * Determines whether two {@link LocalDateTime} instances fall on the same calendar day
     * (time component is ignored).
     *
     * @param dateTime1 the first {@link LocalDateTime} (must not be {@code null})
     * @param dateTime2 the second {@link LocalDateTime} (must not be {@code null})
     * @return {@code true} if both share the same date part
     * @throws NullPointerException if either argument is {@code null}
     */
    static boolean areDatesOnSameDay(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        Objects.requireNonNull(dateTime1, "First LocalDateTime must not be null");
        Objects.requireNonNull(dateTime2, "Second LocalDateTime must not be null");

        return dateTime1.toLocalDate().isEqual(dateTime2.toLocalDate());
    }

    /**
     * Adds the specified number of days to the given {@link LocalDate}.
     *
     * @param date the base {@link LocalDate} (must not be {@code null})
     * @param days the number of days to add (may be negative)
     * @return a new {@link LocalDate} with the days added
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate addDays(LocalDate date, int days) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.plusDays(days);
    }

    /**
     * Adds the specified number of days to the given {@link LocalDateTime}.
     *
     * @param dateTime the base {@link LocalDateTime} (must not be {@code null})
     * @param days     the number of days to add (may be negative)
     * @return a new {@link LocalDateTime} with the days added
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime addDays(LocalDateTime dateTime, int days) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.plusDays(days);
    }

    /**
     * Adds the specified number of months to the given {@link LocalDate}.
     *
     * @param date   the base {@link LocalDate} (must not be {@code null})
     * @param months the number of months to add (may be negative)
     * @return a new {@link LocalDate} with the months added
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate addMonths(LocalDate date, int months) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.plusMonths(months);
    }

    /**
     * Adds the specified number of months to the given {@link LocalDateTime}.
     *
     * @param dateTime the base {@link LocalDateTime} (must not be {@code null})
     * @param months   the number of months to add (may be negative)
     * @return a new {@link LocalDateTime} with the months added
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime addMonths(LocalDateTime dateTime, int months) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.plusMonths(months);
    }

    /**
     * Adds the specified number of years to the given {@link LocalDate}.
     *
     * @param date  the base {@link LocalDate} (must not be {@code null})
     * @param years the number of years to add (may be negative)
     * @return a new {@link LocalDate} with the years added
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate addYears(LocalDate date, int years) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.plusYears(years);
    }

    /**
     * Adds the specified number of years to the given {@link LocalDateTime}.
     *
     * @param dateTime the base {@link LocalDateTime} (must not be {@code null})
     * @param years    the number of years to add (may be negative)
     * @return a new {@link LocalDateTime} with the years added
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime addYears(LocalDateTime dateTime, int years) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.plusYears(years);
    }

    /**
     * Determines whether the specified year is a leap year.
     *
     * @param year the year to check
     * @return {@code true} if the year is a leap year
     */
    static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    /**
     * Returns the start of the week (Monday) for the given {@link LocalDate}.
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return the Monday of the week containing the given date
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate getStartOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.with(DayOfWeek.MONDAY);
    }

    /**
     * Returns the start of the week (Monday at 00:00:00) for the given {@link LocalDateTime}.
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return the Monday of the week at midnight
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime getStartOfWeek(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.with(DayOfWeek.MONDAY).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns the end of the week (Sunday) for the given {@link LocalDate}.
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return the Sunday of the week containing the given date
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate getEndOfWeek(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.with(DayOfWeek.SUNDAY);
    }

    /**
     * Returns the end of the week (Sunday at 23:59:59.999999999) for the given {@link LocalDateTime}.
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return the Sunday of the week at the end of the day
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime getEndOfWeek(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.with(DayOfWeek.SUNDAY).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Returns the first day of the month for the given {@link LocalDate}.
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return the first day of the month
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate getStartOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.withDayOfMonth(1);
    }

    /**
     * Returns the first day of the month (at 00:00:00) for the given {@link LocalDateTime}.
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return the first day of the month at midnight
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime getStartOfMonth(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Returns the last day of the month for the given {@link LocalDate}.
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return the last day of the month
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static LocalDate getEndOfMonth(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * Returns the last day of the month (at 23:59:59.999999999) for the given {@link LocalDateTime}.
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return the last day of the month at the end of the day
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static LocalDateTime getEndOfMonth(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        int lastDay = dateTime.toLocalDate().lengthOfMonth();
        return dateTime.withDayOfMonth(lastDay).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Checks whether the given {@link LocalDate} is today (in the system default timezone).
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return {@code true} if the date is the current date
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static boolean isToday(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.isEqual(LocalDate.now());
    }

    /**
     * Checks whether the given {@link LocalDateTime} falls on today (date part only).
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return {@code true} if the date part is the current date
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static boolean isToday(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.toLocalDate().isEqual(LocalDate.now());
    }

    /**
     * Checks whether the given {@link LocalDate} is in the past (strictly before today).
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return {@code true} if the date is before today
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static boolean isPast(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.isBefore(LocalDate.now());
    }

    /**
     * Checks whether the given {@link LocalDateTime} is in the past (strictly before now).
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return {@code true} if the date-time is before now
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static boolean isPast(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * Checks whether the given {@link LocalDate} is in the future (strictly after today).
     *
     * @param date the {@link LocalDate} (must not be {@code null})
     * @return {@code true} if the date is after today
     * @throws NullPointerException if {@code date} is {@code null}
     */
    static boolean isFuture(LocalDate date) {
        Objects.requireNonNull(date, "LocalDate must not be null");
        return date.isAfter(LocalDate.now());
    }

    /**
     * Checks whether the given {@link LocalDateTime} is in the future (strictly after now).
     *
     * @param dateTime the {@link LocalDateTime} (must not be {@code null})
     * @return {@code true} if the date-time is after now
     * @throws NullPointerException if {@code dateTime} is {@code null}
     */
    static boolean isFuture(LocalDateTime dateTime) {
        Objects.requireNonNull(dateTime, "LocalDateTime must not be null");
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * Returns the later of two {@link LocalDate} values (null-safe).
     *
     * @param date1 the first {@link LocalDate} (may be {@code null})
     * @param date2 the second {@link LocalDate} (may be {@code null})
     * @return the later date, or the non-null argument if one is {@code null}
     */
    static LocalDate max(LocalDate date1, LocalDate date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.isAfter(date2) ? date1 : date2;
    }

    /**
     * Returns the later of two {@link LocalDateTime} values (null-safe).
     *
     * @param dateTime1 the first {@link LocalDateTime} (may be {@code null})
     * @param dateTime2 the second {@link LocalDateTime} (may be {@code null})
     * @return the later date-time, or the non-null argument if one is {@code null}
     */
    static LocalDateTime max(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null) return dateTime2;
        if (dateTime2 == null) return dateTime1;
        return dateTime1.isAfter(dateTime2) ? dateTime1 : dateTime2;
    }

    /**
     * Returns the earlier of two {@link LocalDate} values (null-safe).
     *
     * @param date1 the first {@link LocalDate} (may be {@code null})
     * @param date2 the second {@link LocalDate} (may be {@code null})
     * @return the earlier date, or the non-null argument if one is {@code null}
     */
    static LocalDate min(LocalDate date1, LocalDate date2) {
        if (date1 == null) return date2;
        if (date2 == null) return date1;
        return date1.isBefore(date2) ? date1 : date2;
    }

    /**
     * Returns the earlier of two {@link LocalDateTime} values (null-safe).
     *
     * @param dateTime1 the first {@link LocalDateTime} (may be {@code null})
     * @param dateTime2 the second {@link LocalDateTime} (may be {@code null})
     * @return the earlier date-time, or the non-null argument if one is {@code null}
     */
    static LocalDateTime min(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null) return dateTime2;
        if (dateTime2 == null) return dateTime1;
        return dateTime1.isBefore(dateTime2) ? dateTime1 : dateTime2;
    }

    /**
     * Calculates the number of whole seconds between two {@link LocalDateTime} values.
     *
     * @param start the start {@link LocalDateTime} (must not be {@code null})
     * @param end   the end {@link LocalDateTime} (must not be {@code null})
     * @return the number of seconds between the two instants
     * @throws NullPointerException if either argument is {@code null}
     */
    static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * Calculates the number of whole hours between two {@link LocalDateTime} values.
     *
     * @param start the start {@link LocalDateTime} (must not be {@code null})
     * @param end   the end {@link LocalDateTime} (must not be {@code null})
     * @return the number of hours between the two instants
     * @throws NullPointerException if either argument is {@code null}
     */
    static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculates the number of whole minutes between two {@link LocalDateTime} values.
     *
     * @param start the start {@link LocalDateTime} (must not be {@code null})
     * @param end   the end {@link LocalDateTime} (must not be {@code null})
     * @return the number of minutes between the two instants
     * @throws NullPointerException if either argument is {@code null}
     */
    static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Calculates the number of whole days between two {@link LocalDate} values.
     *
     * @param start the start {@link LocalDate} (must not be {@code null})
     * @param end   the end {@link LocalDate} (must not be {@code null})
     * @return the number of days between the two dates
     * @throws NullPointerException if either argument is {@code null}
     */
    static long daysBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start LocalDate must not be null");
        Objects.requireNonNull(end, "End LocalDate must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates the number of whole days between two {@link LocalDateTime} values
     * (time components are ignored for the day count).
     *
     * @param start the start {@link LocalDateTime} (must not be {@code null})
     * @param end   the end {@link LocalDateTime} (must not be {@code null})
     * @return the number of days between the two dates
     * @throws NullPointerException if either argument is {@code null}
     */
    static long daysBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates the number of whole months between two {@link LocalDate} values.
     *
     * @param start the start {@link LocalDate} (must not be {@code null})
     * @param end   the end {@link LocalDate} (must not be {@code null})
     * @return the number of months between the two dates
     * @throws NullPointerException if either argument is {@code null}
     */
    static long monthsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start LocalDate must not be null");
        Objects.requireNonNull(end, "End LocalDate must not be null");
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calculates the number of whole months between two {@link LocalDateTime} values.
     *
     * @param start the start {@link LocalDateTime} (must not be {@code null})
     * @param end   the end {@link LocalDateTime} (must not be {@code null})
     * @return the number of months between the two dates
     * @throws NullPointerException if either argument is {@code null}
     */
    static long monthsBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.MONTHS.between(start, end);
    }

    /**
     * Calculates the number of whole years between two {@link LocalDate} values.
     *
     * @param start the start {@link LocalDate} (must not be {@code null})
     * @param end   the end {@link LocalDate} (must not be {@code null})
     * @return the number of years between the two dates
     * @throws NullPointerException if either argument is {@code null}
     */
    static long yearsBetween(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Start LocalDate must not be null");
        Objects.requireNonNull(end, "End LocalDate must not be null");
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Calculates the number of whole years between two {@link LocalDateTime} values.
     *
     * @param start the start {@link LocalDateTime} (must not be {@code null})
     * @param end   the end {@link LocalDateTime} (must not be {@code null})
     * @return the number of years between the two dates
     * @throws NullPointerException if either argument is {@code null}
     */
    static long yearsBetween(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start LocalDateTime must not be null");
        Objects.requireNonNull(end, "End LocalDateTime must not be null");
        return ChronoUnit.YEARS.between(start, end);
    }

    /**
     * Converts a {@link LocalDateTime} to a legacy {@link java.util.Date} using the system default zone.
     *
     * @param dateTime the {@link LocalDateTime} to convert (may be {@code null})
     * @return the corresponding {@link Date}, or {@code null} if the input is {@code null}
     */
    static Date localDateTimetoLegacyDate(LocalDateTime dateTime) {
        if (dateTime != null) {
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Converts a {@link LocalDate} to a legacy {@link java.util.Date} at the start of the day
     * using the system default zone.
     *
     * @param date the {@link LocalDate} to convert (may be {@code null})
     * @return the corresponding {@link Date} at midnight, or {@code null} if the input is {@code null}
     */
    static Date localDatetoLegacyDate(LocalDate date) {
        if (date != null) {
            return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Converts a legacy {@link java.util.Date} to a {@link LocalDateTime} using the system default zone.
     *
     * @param date the {@link Date} to convert (may be {@code null})
     * @return the corresponding {@link LocalDateTime}, or {@code null} if the input is {@code null}
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
     * Converts a legacy {@link java.util.Date} to a {@link LocalDate} (time component discarded)
     * using the system default zone.
     *
     * @param date the {@link Date} to convert (may be {@code null})
     * @return the corresponding {@link LocalDate}, or {@code null} if the input is {@code null}
     */
    static LocalDate legacyDatetoLocalDate(Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }

    // ==================== DEPRECATED METHODS - java.util.Date (retro-compatible) ====================

    /**
     * @deprecated Use {@link #parseLocalDateTime(String, LocalDateTime)} instead.
     *             Retained for backward compatibility; parses using the same patterns
     *             but returns a legacy {@link Date} (instant-based semantics).
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
     * @deprecated Use {@link #formatToIsoString(LocalDateTime)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static String formatDateToIsoString(Date date) {
        return formatToIsoString(legacyDatetoLocalDateTime(date));
    }

    /**
     * @deprecated Use {@link #occurredInLastXHours(LocalDateTime, int)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean occurredInLastXHours(Date date, int hours) {
        Objects.requireNonNull(date, "Date must not be null");
        LocalDateTime ldt = legacyDatetoLocalDateTime(date);
        return occurredInLastXHours(ldt, hours);
    }

    /**
     * @deprecated Use {@link #areDatesOnSameDay(LocalDate, LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean areDatesOnSameDay(Date date1, Date date2) {
        Objects.requireNonNull(date1, "First date must not be null");
        Objects.requireNonNull(date2, "Second date must not be null");

        return areDatesOnSameDay(
                legacyDatetoLocalDate(date1),
                legacyDatetoLocalDate(date2)
        );
    }

    /**
     * @deprecated Use {@link #addDays(LocalDate, int)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date addDaysToDate(Date date, int days) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate ld = legacyDatetoLocalDate(date);
        LocalDate newLd = addDays(ld, days);
        return localDatetoLegacyDate(newLd);
    }

    /**
     * @deprecated Use {@link #addMonths(LocalDate, int)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date addMonthsToDate(Date date, int months) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate ld = legacyDatetoLocalDate(date);
        LocalDate newLd = addMonths(ld, months);
        return localDatetoLegacyDate(newLd);
    }

    /**
     * @deprecated Use {@link #getStartOfWeek(LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date getStartOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate ld = legacyDatetoLocalDate(date);
        LocalDate start = getStartOfWeek(ld);
        return localDatetoLegacyDate(start);
    }

    /**
     * @deprecated Use {@link #getEndOfWeek(LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date getEndOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate ld = legacyDatetoLocalDate(date);
        LocalDate end = getEndOfWeek(ld);
        return localDatetoLegacyDate(end);
    }

    /**
     * @deprecated Use {@link #formatToHumanReadable(LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static String formatDateToHumanReadable(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        LocalDate ld = legacyDatetoLocalDate(date);
        return formatToHumanReadable(ld);
    }

    /**
     * @deprecated Use {@link #isToday(LocalDate)} or {@link #areDatesOnSameDay(LocalDate, LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean isToday(Date date) {
        return areDatesOnSameDay(date, new Date());
    }

    /**
     * @deprecated Use {@link #clearTime(LocalDateTime)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date clearTime(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime ldt = legacyDatetoLocalDateTime(date);
        LocalDateTime cleared = clearTime(ldt);
        return localDateTimetoLegacyDate(cleared);
    }

    /**
     * Clears the time component of a {@link LocalDateTime} (sets to midnight).
     *
     * @param dateTime the {@link LocalDateTime} (may be {@code null})
     * @return the date-time with time set to 00:00:00.000000000, or {@code null} if input was {@code null}
     */
    static LocalDateTime clearTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Clears the time component of a {@link LocalDate} (no-op, since {@link LocalDate} has no time).
     *
     * @param date the {@link LocalDate} (may be {@code null})
     * @return the same {@link LocalDate}, or {@code null} if input was {@code null}
     */
    static LocalDate clearTime(LocalDate date) {
        return date;
    }

    /**
     * @deprecated Use {@link #hasTime(LocalDateTime)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static boolean hasTime(Date date) {
        if (date == null) {
            return false;
        }
        LocalDateTime ldt = legacyDatetoLocalDateTime(date);
        return hasTime(ldt);
    }

    /**
     * Checks whether a {@link LocalDateTime} has a non-zero time component.
     *
     * @param dateTime the {@link LocalDateTime} (may be {@code null})
     * @return {@code true} if hour, minute, second, or nano is greater than zero
     */
    static boolean hasTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        return dateTime.getHour() > 0 || dateTime.getMinute() > 0 ||
                dateTime.getSecond() > 0 || dateTime.getNano() > 0;
    }

    /**
     * Checks whether a {@link LocalDate} has a time component.
     * Always returns {@code false} because {@link LocalDate} has no time.
     *
     * @param date the {@link LocalDate} (ignored)
     * @return always {@code false}
     */
    static boolean hasTime(LocalDate date) {
        return false;
    }

    /**
     * @deprecated Use {@link #max(LocalDate, LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date max(Date d1, Date d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;

        LocalDateTime ld1 = legacyDatetoLocalDateTime(d1);
        LocalDateTime ld2 = legacyDatetoLocalDateTime(d2);
        LocalDateTime maxLd = max(ld1, ld2);
        return localDateTimetoLegacyDate(maxLd);
    }

    /**
     * @deprecated Use {@link #min(LocalDate, LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date min(Date d1, Date d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;

        LocalDateTime ld1 = legacyDatetoLocalDateTime(d1);
        LocalDateTime ld2 = legacyDatetoLocalDateTime(d2);
        LocalDateTime minLd = min(ld1, ld2);
        return localDateTimetoLegacyDate(minLd);
    }

    /**
     * @deprecated Use {@link #secondsBetween(LocalDateTime, LocalDateTime)} instead
     *             (or the more specific {@code *Between} methods).
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static long between(Date lowDate, Date highDate) {
        LocalDateTime llow = legacyDatetoLocalDateTime(lowDate);
        LocalDateTime lhigh = legacyDatetoLocalDateTime(highDate);
        return secondsBetween(llow, lhigh);
    }

    /**
     * @deprecated Use {@link #localDateTimetoLegacyDate(LocalDateTime)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date toLegacyDate(LocalDateTime localDateTime) {
        return localDateTimetoLegacyDate(localDateTime);
    }

    /**
     * @deprecated Use {@link #localDatetoLegacyDate(LocalDate)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static Date toLegacyDate(LocalDate localDate) {
        return localDatetoLegacyDate(localDate);
    }

    /**
     * @deprecated Use {@link #legacyDatetoLocalDateTime(Date)} instead.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    static LocalDateTime toLocalDateTime(Date date) {
        return legacyDatetoLocalDateTime(date);
    }
}