package org.xpenbox.common;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.jboss.logging.Logger;

/**
 * Utility class for date conversion.
 */
public class DateFunctions {
    private static final Logger LOG = Logger.getLogger(DateFunctions.class);

    /**
     * Converts a LocalDateTime to a timestamp in milliseconds.
     * @param dateTime The LocalDateTime to be converted.
     * @return The corresponding timestamp in milliseconds, or null if the input is null.
     */
    public static Long convertToTimestamp(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to timestamp");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Converts a timestamp in milliseconds to a LocalDateTime.
     * @param timestamp The timestamp in milliseconds to be converted.
     * @return The corresponding LocalDateTime, or null if the input is null.
     */
    public static LocalDateTime convertToLocalDateTime(Long timestamp) {
        LOG.debug("Converting timestamp to LocalDateTime");

        if (timestamp == null) {
            LOG.warn("Provided timestamp is null, returning null");
            return null;
        }

        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Gets the current LocalDateTime.
     * @return The current LocalDateTime.
     */
    public static LocalDateTime currentLocalDateTime() {
        LOG.debug("Getting current LocalDateTime");
        return LocalDateTime.now();
    }

    /**
     * Gets the current timestamp in milliseconds.
     * @return The current timestamp in milliseconds.
     */
    public static Long currentTimestamp() {
        LOG.debug("Getting current timestamp");
        return convertToTimestamp(currentLocalDateTime());
    }

    /**
     * Calculates the duration between two LocalDateTime instances.
     * @param start The start LocalDateTime.
     * @param end The end LocalDateTime.
     * @return The Duration between the two LocalDateTime instances, or null if either input is null.
     */
    public static Duration calculateDuration(LocalDateTime start, LocalDateTime end) {
        LOG.debug("Calculating duration between two LocalDateTime instances");

        if (start == null || end == null) {
            LOG.warn("One or both LocalDateTime instances are null, returning null");
            return null;
        }

        return Duration.between(start, end);
    }

    /**
     * Converts a LocalDateTime to the first day of its year.
     * @param dateTime The LocalDateTime to be converted.
     * @return The LocalDateTime representing the first day of the year of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toFirstDayOfYear(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to first day of year");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Converts a LocalDate to the first day of its year.
     * @param dateTime The LocalDate to be converted.
     * @return The LocalDateTime representing the first day of the year of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toFirstDayOfYear(LocalDate dateTime) {
        LOG.debug("Converting LocalDate to first day of year");

        if (dateTime == null) {
            LOG.warn("Provided LocalDate is null, returning null");
            return null;
        }

        return dateTime.withDayOfYear(1).atStartOfDay();
    }
    
    /**
     * Converts a LocalDateTime to the last day of its year.
     * @param dateTime The LocalDateTime to be converted.
     * @return The LocalDateTime representing the last day of the year of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toLastDayOfYear(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to last day of year");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.withDayOfYear(dateTime.toLocalDate().lengthOfYear()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Converts a LocalDate to the last day of its year.
     * @param dateTime The LocalDate to be converted.
     * @return The LocalDateTime representing the last day of the year of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toLastDayOfYear(LocalDate dateTime) {
        LOG.debug("Converting LocalDate to last day of year");

        if (dateTime == null) {
            LOG.warn("Provided LocalDate is null, returning null");
            return null;
        }

        return dateTime.withDayOfYear(dateTime.lengthOfYear()).atTime(23, 59, 59, 999999999);
    }

    /**
     * Converts a LocalDateTime to the first day of its month.
     * @param dateTime The LocalDateTime to be converted.
     * @return The LocalDateTime representing the first day of the month of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toFirstDayOfMonth(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to first day of month");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Converts a LocalDate to the first day of its month.
     * @param dateTime The LocalDate to be converted.
     * @return The LocalDateTime representing the first day of the month of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toFirstDayOfMonth(LocalDate dateTime) {
        LOG.debug("Converting LocalDate to first day of month");

        if (dateTime == null) {
            LOG.warn("Provided LocalDate is null, returning null");
            return null;
        }

        return dateTime.withDayOfMonth(1).atStartOfDay();
    }

    /**
     * Converts a LocalDateTime to the last day of its month.
     * @param dateTime The LocalDateTime to be converted.
     * @return The LocalDateTime representing the last day of the month of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toLastDayOfMonth(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to last day of month");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.withDayOfMonth(dateTime.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Converts a LocalDate to the last day of its month.
     * @param dateTime The LocalDate to be converted.
     * @return The LocalDateTime representing the last day of the month of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toLastDayOfMonth(LocalDate dateTime) {
        LOG.debug("Converting LocalDate to last day of month");

        if (dateTime == null) {
            LOG.warn("Provided LocalDate is null, returning null");
            return null;
        }

        return dateTime.withDayOfMonth(dateTime.lengthOfMonth()).atTime(23, 59, 59, 999999999);
    }

    /**
     * Converts a LocalDateTime to the start of its day (00:00:00.000).
     * @param dateTime The LocalDateTime to be converted.
     * @return The LocalDateTime representing the start of the day of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toStartDay(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to start of day");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    /**
     * Converts a LocalDate to the start of its day (00:00:00.000).
     * @param dateTime The LocalDate to be converted.
     * @return The LocalDateTime representing the start of the day of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toStartDay(LocalDate dateTime) {
        LOG.debug("Converting LocalDate to start of day");

        if (dateTime == null) {
            LOG.warn("Provided LocalDate is null, returning null");
            return null;
        }

        return dateTime.atStartOfDay();
    }

    /**
     * Converts a LocalDateTime to the end of its day (23:59:59.999).
     * @param dateTime The LocalDateTime to be converted.
     * @return The LocalDateTime representing the end of the day of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toEndDay(LocalDateTime dateTime) {
        LOG.debug("Converting LocalDateTime to end of day");

        if (dateTime == null) {
            LOG.warn("Provided LocalDateTime is null, returning null");
            return null;
        }

        return dateTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }

    /**
     * Converts a LocalDate to the end of its day (23:59:59.999).
     * @param dateTime The LocalDate to be converted.
     * @return The LocalDateTime representing the end of the day of the input dateTime, or null if the input is null.
     */
    public static LocalDateTime toEndDay(LocalDate dateTime) {
        LOG.debug("Converting LocalDate to end of day");

        if (dateTime == null) {
            LOG.warn("Provided LocalDate is null, returning null");
            return null;
        }

        return dateTime.atTime(23, 59, 59, 999999999);
    }

    /**
     * Converts a date-time string to a LocalDateTime using the provided DateTimeFormatter. The method parses the input string according to the specified formatter and converts it to the system's default time zone.
     * @param dateTimeStr The date-time string to be converted, which should be in a format compatible with the provided DateTimeFormatter.
     * @param formatter The DateTimeFormatter to be used for parsing the date-time string, which defines the expected format of the input string.
     * @return The LocalDateTime representation of the input date-time string, or null if the input string or formatter is null, or if there is an error during parsing.
     */
    public static LocalDateTime toDateTimeFormatter(String dateTimeStr, DateTimeFormatter formatter) {
        LOG.debug("Converting String to LocalDateTime using provided DateTimeFormatter");

        if (dateTimeStr == null || formatter == null) {
            LOG.warn("Provided dateTimeStr or formatter is null, returning null");
            return null;
        }

        try {
            return OffsetDateTime.parse(dateTimeStr, formatter).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            LOG.errorf(e, "Error parsing date string: %s with formatter: %s", dateTimeStr, formatter);
            return null;
        }
    }
}
