package eu.isygoit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * The interface Date helper.
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
     * The constant CALENDAR_START.
     */
    LocalTime TIME_START = LocalTime.of(0, 0, 0, 0);
    /**
     * The constant CALENDAR_END.
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

    /**
     * Parse date string date.
     *
     * @param dateString    the date string
     * @param defaultIfNull the default if null
     * @return the date
     */
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
            // .withResolverStyle(ResolverStyle.STRICT);
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
                // Validation for strict mode
                try {
                    // Try to re-format and compare
                    // This is the most reliable way to ensure the date is what we think it is
                    // especially for things like Feb 31 -> Mar 3

                    // For patterns with 'z', we should format with the original zone if possible
                    // However, 'result' is a Date which is just an instant.
                    // If we use systemDefault(), it might change GMT to CET/CEST etc.

                    ZoneId zoneId = ZoneId.systemDefault();
                    boolean skipStrict = false;
                    if (pattern.contains("z") || pattern.contains("VV") || pattern.contains("X") || pattern.contains("x")) {
                        // Extract zone if possible, or just skip strict validation for these for now
                        // or use the zone from temporalAccessor
                        try {
                            zoneId = ZoneId.from(temporalAccessor);
                        } catch (Exception e) {
                            // Fallback to system default or UTC
                            zoneId = ZoneId.of("UTC");
                        }
                        // Skip strict validation for zone patterns because re-formatting might change GMT to UTC or vice versa
                        // or other zone name variations that are semantically identical but string-different
                        skipStrict = true;
                    }

                    if (!skipStrict) {
                        String reformatted = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)
                                .format(result.toInstant().atZone(zoneId));

                        // If they don't match, it might be an invalid date that was resolved (e.g. Feb 31)
                        if (!dateString.equalsIgnoreCase(reformatted)) {
                            return null;
                        }
                    }
                } catch (Exception e) {
                    // If we can't format it back, something is wrong
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
     * Format date to iso string string.
     *
     * @param date the date
     * @return the string
     */
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
     * Occurred in last x hours boolean.
     *
     * @param date  the date
     * @param hours the hours
     * @return the boolean
     */
    static boolean occurredInLastXHours(Date date, int hours) {
        Objects.requireNonNull(date, "Date must not be null");

        Instant threshold = Instant.now().minus(hours, ChronoUnit.HOURS);
        boolean isRecent = date.toInstant().isAfter(threshold);

        logger.debug("Checking if date {} occurred in the last {} hours. Threshold: {}, Result: {}", date, hours, threshold, isRecent);
        return isRecent;
    }

    /**
     * Are dates on same day boolean.
     *
     * @param date1 the date 1
     * @param date2 the date 2
     * @return the boolean
     */
    static boolean areDatesOnSameDay(Date date1, Date date2) {
        Objects.requireNonNull(date1, "First date must not be null");
        Objects.requireNonNull(date2, "Second date must not be null");

        return date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                .isEqual(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * Add days to date date.
     *
     * @param date the date
     * @param days the days
     * @return the date
     */
    static Date addDaysToDate(Date date, int days) {
        Objects.requireNonNull(date, "Date must not be null");

        var newDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusDays(days);

        return Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Add months to date date.
     *
     * @param date   the date
     * @param months the months
     * @return the date
     */
    static Date addMonthsToDate(Date date, int months) {
        Objects.requireNonNull(date, "Date must not be null");

        var newDate = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .plusMonths(months);

        return Date.from(newDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Is leap year boolean.
     *
     * @param year the year
     * @return the boolean
     */
    static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    /**
     * Gets start of week.
     *
     * @param date the date
     * @return the start of week
     */
    static Date getStartOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        var startOfWeek = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .with(DayOfWeek.MONDAY);

        return Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Gets end of week.
     *
     * @param date the date
     * @return the end of week
     */
    static Date getEndOfWeek(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        var endOfWeek = date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .with(DayOfWeek.SUNDAY);

        return Date.from(endOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Format date to human readable string.
     *
     * @param date the date
     * @return the string
     */
    static String formatDateToHumanReadable(Date date) {
        Objects.requireNonNull(date, "Date must not be null");

        return DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
                .format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    /**
     * Is today boolean.
     *
     * @param date the date
     * @return the boolean
     */
    static boolean isToday(Date date) {
        return areDatesOnSameDay(date, new Date());
    }

    /**
     * Clear time date.
     *
     * @param date the date
     * @return the date
     */
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
     * Has time boolean.
     *
     * @param date the date
     * @return the boolean
     */
    static boolean hasTime(Date date) {
        if (date == null) return false;

        var calendar = Calendar.getInstance();
        calendar.setTime(date);

        return calendar.get(Calendar.HOUR_OF_DAY) > 0 || calendar.get(Calendar.MINUTE) > 0 ||
                calendar.get(Calendar.SECOND) > 0 || calendar.get(Calendar.MILLISECOND) > 0;
    }

    /**
     * Max date.
     *
     * @param d1 the d 1
     * @param d2 the d 2
     * @return the date
     */
    static Date max(Date d1, Date d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.after(d2) ? d1 : d2;
    }

    /**
     * Min date.
     *
     * @param d1 the d 1
     * @param d2 the d 2
     * @return the date
     */
    static Date min(Date d1, Date d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.before(d2) ? d1 : d2;
    }

    /**
     * Between long.
     *
     * @param lowDate  the low date
     * @param highDate the high date
     * @return the long
     */
    static long between(Date lowDate, Date highDate) {
        return ChronoUnit.SECONDS.between(lowDate.toInstant(), highDate.toInstant());
    }

    /**
     * To date date.
     *
     * @param localDateTime the local date time
     * @return the date
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Convert to local date time java . time . local date time.
     *
     * @param date the date to convert
     * @return the java . time . local date time
     */
    public static java.time.LocalDateTime toLocalDateTime(Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }
}