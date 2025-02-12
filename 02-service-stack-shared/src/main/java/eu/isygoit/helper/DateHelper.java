package eu.isygoit.helper;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.xml.datatype.Duration;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The type Date helper.
 */
public interface DateHelper {

    Logger logger = LoggerFactory.getLogger(DateHelper.class);

    /**
     * The constant BEGIN_OF_CALENDAR.
     */
    public static final LocalDateTime BEGIN_OF_CALENDAR = new LocalDateTime(1900, 01, 01, 0, 0, 0);
    /**
     * The constant END_OF_CALENDAR.
     */
    public static final LocalDateTime END_OF_CALENDAR = new LocalDateTime(2999, 12, 31, 0, 0, 0);

    /**
     * The constant DATE_WITH_TIME_ZONE_RFC_EN.
     */
    public static final String DATE_WITH_TIME_ZONE_RFC_EN = "yyyy-MM-dd'E'HH:mm:ss.SSSZZ";
    /**
     * The constant DATE_WITH_TIME_ZONE_ISO_EN.
     */
    public static final String DATE_WITH_TIME_ZONE_ISO_EN = "yyyy-MM-dd'E'HH:mm:ss.SSSXXX";
    /**
     * The constant DATE_WITH_TIME_ZONE_RFC_FR.
     */
    public static final String DATE_WITH_TIME_ZONE_RFC_FR = "dd-MM-yyyy'E'HH:mm:ss.SSSZZ";
    /**
     * The constant DATE_WITH_TIME_ZONE_ISO_FR.
     */
    public static final String DATE_WITH_TIME_ZONE_ISO_FR = "dd-MM-yyyy'E'HH:mm:ss.SSSXXX";
    /**
     * The constant DATE_WITH_TIME_EN.
     */
    public static final String DATE_WITH_TIME_EN = "yyyy-MM-dd'E'HH:mm";
    /**
     * The constant DATE_WITH_TIME_FR.
     */
    public static final String DATE_WITH_TIME_FR = "dd-MM-yyyy'E'HH:mm";
    /**
     * The constant SIMPLE_DATE_FORMAT_FR.
     */
    public static final String SIMPLE_DATE_FORMAT_FR = "dd/MM/yyyy";
    /**
     * The constant SIMPLE_DATE_FORMAT_EN.
     */
    public static final String SIMPLE_DATE_FORMAT_EN = "yyyy/MM/dd";
    /**
     * The constant URL_DATE_FORMAT_FR.
     */
    public static final String URL_DATE_FORMAT_FR = "dd-MM-yyyy";
    /**
     * The constant URL_DATE_FORMAT_EN.
     */
    public static final String URL_DATE_FORMAT_EN = "yyyy-MM-dd";
    /**
     * The constant COMPACT_DATE_FORMAT_FR.
     */
    public static final String COMPACT_DATE_FORMAT_FR = "ddMMyyyy";
    /**
     * The constant COMPACT_DATE_FORMAT_EN.
     */
    public static final String COMPACT_DATE_FORMAT_EN = "yyyyMMdd";
    /**
     * The constant TIME_HHmm.
     */
    public static final String TIME_HHmm = "HH:mm";
    /**
     * The constant MAX_DATE.
     */
    public static Date MAX_DATE = new Date(Long.MAX_VALUE);

    /**
     * Convert absolute date to date date.
     *
     * @param absDate   the abs date
     * @param defIfNull the def if null
     * @return the date
     * @throws ParseException the parse exception
     */
    public static Date convertAbsoluteDateToDate(String absDate, Date defIfNull) throws ParseException {
        if (!StringUtils.hasText(absDate) || "null".equals(absDate)) {
            return defIfNull;
        }

        final List<SimpleDateFormat> knownPatterns = new ArrayList<>();
        knownPatterns.add(new SimpleDateFormat(DATE_WITH_TIME_ZONE_ISO_EN));
        knownPatterns.add(new SimpleDateFormat(DATE_WITH_TIME_ZONE_ISO_FR));
        knownPatterns.add(new SimpleDateFormat(DATE_WITH_TIME_ZONE_RFC_EN));
        knownPatterns.add(new SimpleDateFormat(DATE_WITH_TIME_ZONE_RFC_FR));
        knownPatterns.add(new SimpleDateFormat(URL_DATE_FORMAT_EN));
        knownPatterns.add(new SimpleDateFormat(URL_DATE_FORMAT_FR));
        knownPatterns.add(new SimpleDateFormat(SIMPLE_DATE_FORMAT_EN));
        knownPatterns.add(new SimpleDateFormat(SIMPLE_DATE_FORMAT_FR));
        knownPatterns.add(new SimpleDateFormat(COMPACT_DATE_FORMAT_EN));
        knownPatterns.add(new SimpleDateFormat(COMPACT_DATE_FORMAT_FR));
        for (final SimpleDateFormat pattern : knownPatterns) {
            try {
                return pattern.parse(absDate);
            } catch (final Exception e) {

            }
        }

        throw new IllegalArgumentException();
    }

    /**
     * Convert date to absole date string.
     *
     * @param date the date
     * @return the string
     */
    public static String convertDateToAbsoleDate(Date date) {
        if (date != null) {
            final DateTime dateTime = new DateTime(date);
            final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_WITH_TIME_ZONE_ISO_FR);
            return dateTime.toString(dateTimeFormatter);
        }
        return null;
    }

    /**
     * Convert date to simple date string.
     *
     * @param date       the date
     * @param dateFormat the date format
     * @return the string
     */
    public static String convertDateToSimpleDate(Date date, String dateFormat) {
        if (date != null) {
            final DateTime dateTime = new DateTime(date);
            final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(dateFormat /*SIMPLE_DATE_FORMAT_FR*/);
            return dateTime.toString(dateTimeFormatter);
        }
        return null;
    }

    /**
     * Duration to seconds int.
     *
     * @param duration the duration
     * @return the int
     */
    public static int durationToSeconds(Duration duration) {
        final int hours = duration.getHours();
        final int days = duration.getDays();
        final int minutes = duration.getMinutes();
        final int seconds = duration.getSeconds();
        return seconds + minutes * 60 + hours * 3600 + days * 86400;
    }

    /**
     * Sets first time.
     *
     * @param date the date
     * @return the first time
     */
    public static Date setFirstTime(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.fromDateFields(date);
        return dateTime.withTime(0, 0, 0, 0).toDate();
    }

    /**
     * Sets last time.
     *
     * @param date the date
     * @return the last time
     */
    public static Date setLastTime(Date date) {
        if (date == null) {
            return null;
        }
        LocalDateTime dateTime = LocalDateTime.fromDateFields(date);
        return dateTime.withTime(23, 59, 0, 0).toDate();
    }

    /**
     * Is in last hours boolean.
     *
     * @param date  the date
     * @param hours the hours
     * @return the boolean
     */
    public static boolean isInLastHours(Date date, Integer hours) {
        if (date == null || hours == null) {
            throw new IllegalArgumentException("Date and hours must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -hours);
        return date.after(cal.getTime());
    }

    /**
     * Is same day boolean.
     *
     * @param date1 the date 1
     * @param date2 the date 2
     * @return the boolean
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * Is same day boolean.
     *
     * @param cal1 the cal 1
     * @param cal2 the cal 2
     * @return the boolean
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * Is today boolean.
     *
     * @param date the date
     * @return the boolean
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    /**
     * Is today boolean.
     *
     * @param cal the cal
     * @return the boolean
     */
    public static boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    /**
     * Is before day boolean.
     *
     * @param date1 the date 1
     * @param date2 the date 2
     * @return the boolean
     */
    public static boolean isBeforeDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isBeforeDay(cal1, cal2);
    }

    /**
     * Is before day boolean.
     *
     * @param cal1 the cal 1
     * @param cal2 the cal 2
     * @return the boolean
     */
    public static boolean isBeforeDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return true;
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return false;
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true;
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false;
        return cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Is after day boolean.
     *
     * @param date1 the date 1
     * @param date2 the date 2
     * @return the boolean
     */
    public static boolean isAfterDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isAfterDay(cal1, cal2);
    }

    /**
     * Is after day boolean.
     *
     * @param cal1 the cal 1
     * @param cal2 the cal 2
     * @return the boolean
     */
    public static boolean isAfterDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return false;
        if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return true;
        if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return false;
        if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return true;
        return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Is within days future boolean.
     *
     * @param date the date
     * @param days the days
     * @return the boolean
     */
    public static boolean isWithinDaysFuture(Date date, int days) {
        if (date == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return isWithinDaysFuture(cal, days);
    }

    /**
     * Is within days future boolean.
     *
     * @param cal  the cal
     * @param days the days
     * @return the boolean
     */
    public static boolean isWithinDaysFuture(Calendar cal, int days) {
        if (cal == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        Calendar today = Calendar.getInstance();
        Calendar future = Calendar.getInstance();
        future.add(Calendar.DAY_OF_YEAR, days);
        return (isAfterDay(cal, today) && !isAfterDay(cal, future));
    }

    /**
     * Gets start.
     *
     * @param date the date
     * @return the start
     */
    public static Date getStart(Date date) {
        return clearTime(date);
    }

    /**
     * Clear time date.
     *
     * @param date the date
     * @return the date
     */
    public static Date clearTime(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * Has time boolean.
     *
     * @param date the date
     * @return the boolean
     */
    public static boolean hasTime(Date date) {
        if (date == null) {
            return false;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        if (c.get(Calendar.HOUR_OF_DAY) > 0) {
            return true;
        }
        if (c.get(Calendar.MINUTE) > 0) {
            return true;
        }
        if (c.get(Calendar.SECOND) > 0) {
            return true;
        }
        return c.get(Calendar.MILLISECOND) > 0;
    }

    /**
     * Gets end.
     *
     * @param date the date
     * @return the end
     */
    public static Date getEnd(Date date) {
        if (date == null) {
            return null;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTime();
    }

    /**
     * Max date.
     *
     * @param d1 the d 1
     * @param d2 the d 2
     * @return the date
     */
    public static Date max(Date d1, Date d2) {
        if (d1 == null && d2 == null) return null;
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return (d1.after(d2)) ? d1 : d2;
    }

    /**
     * Min date.
     *
     * @param d1 the d 1
     * @param d2 the d 2
     * @return the date
     */
    public static Date min(Date d1, Date d2) {
        if (d1 == null && d2 == null) return null;
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return (d1.before(d2)) ? d1 : d2;
    }

    /**
     * To date date.
     *
     * @param localDateTime the local date time
     * @return the date
     */
    public static Date toDate(java.time.LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * Convert to local date time java . time . local date time.
     *
     * @param dateToConvert the date to convert
     * @return the java . time . local date time
     */
    public static java.time.LocalDateTime convertToLocalDateTime(Date dateToConvert) {
        if (dateToConvert != null) {
            return Instant.ofEpochMilli(dateToConvert.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }
        return null;
    }

    /**
     * Convert to local date java . time . local date.
     *
     * @param dateToConvert the date to convert
     * @return the java . time . local date
     */
    public static java.time.LocalDate convertToLocalDate(Date dateToConvert) {
        if (dateToConvert != null) {
            return Instant.ofEpochMilli(dateToConvert.getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return null;
    }

    /**
     * Between long.
     *
     * @param lowDate  the low date
     * @param highDate the high date
     * @return the long
     */
    public static long between(Date lowDate, Date highDate) {
        return ChronoUnit.SECONDS.between(lowDate.toInstant(), highDate.toInstant());
    }
}
