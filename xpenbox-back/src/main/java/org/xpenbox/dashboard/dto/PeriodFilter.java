package org.xpenbox.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.xpenbox.exception.BadRequestException;

/**
 * Enum representing the different period filters that can be applied to the dashboard data.
 */
public enum PeriodFilter {
    CURRENT_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    LAST_6_MONTHS,
    CURRENT_YEAR,
    LAST_YEAR;    

    /**
     * Returns the date range corresponding to the given period filter.
     * @param filter the period filter for which to calculate the date range
     * @return a map containing the "from" and "to" LocalDateTime values for the specified period filter
     */
    public static Map<String, LocalDateTime> getDateRange(PeriodFilter filter) {
        LocalDate now = LocalDate.now();
        switch (filter) {
            case CURRENT_MONTH:
                return Map.of("from", now.withDayOfMonth(1).atStartOfDay(), "to", now.atTime(23, 59, 59));
            case LAST_MONTH:
                LocalDateTime lastMonthStart = now.minusMonths(1).withDayOfMonth(1).atStartOfDay();
                LocalDateTime lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);
                return Map.of("from", lastMonthStart, "to", lastMonthEnd);
            case LAST_3_MONTHS:
                LocalDateTime threeMonthsAgo = now.minusMonths(3).withDayOfMonth(1).atStartOfDay();
                return Map.of("from", threeMonthsAgo, "to", now.atTime(23, 59, 59));
            case LAST_6_MONTHS:
                LocalDateTime sixMonthsAgo = now.minusMonths(6).withDayOfMonth(1).atStartOfDay();
                return Map.of("from", sixMonthsAgo, "to", now.atTime(23, 59, 59));
            case CURRENT_YEAR:
                return Map.of("from", now.withDayOfYear(1).atStartOfDay(), "to", now.atTime(23, 59, 59));
            case LAST_YEAR:
                LocalDateTime lastYearStart = now.minusYears(1).withDayOfYear(1).atStartOfDay();
                LocalDateTime lastYearEnd = lastYearStart.withDayOfYear(lastYearStart.toLocalDate().lengthOfYear()).withHour(23).withMinute(59).withSecond(59);
                return Map.of("from", lastYearStart, "to", lastYearEnd);
            default:
                throw new BadRequestException("Unknown period filter: " + filter);
        }
    }
}
