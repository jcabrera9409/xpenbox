package org.xpenbox.dashboard.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import org.xpenbox.common.DateFunctions;
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
        LocalDate now = DateFunctions.currentLocalDateTime().toLocalDate();
        switch (filter) {
            case CURRENT_MONTH:
                return Map.of("from", DateFunctions.toFirstDayOfMonth(now), "to", DateFunctions.toEndDay(now));
            case LAST_MONTH:
                LocalDateTime lastMonthStart = DateFunctions.toFirstDayOfMonth(now.minusMonths(1));
                LocalDateTime lastMonthEnd = DateFunctions.toLastDayOfMonth(now.minusMonths(1));
                return Map.of("from", lastMonthStart, "to", lastMonthEnd);
            case LAST_3_MONTHS:
                LocalDateTime threeMonthsAgo = DateFunctions.toFirstDayOfMonth(now.minusMonths(3));
                return Map.of("from", threeMonthsAgo, "to", DateFunctions.toEndDay(now));
            case LAST_6_MONTHS:
                LocalDateTime sixMonthsAgo = DateFunctions.toFirstDayOfMonth(now.minusMonths(6));
                return Map.of("from", sixMonthsAgo, "to", DateFunctions.toEndDay(now));
            case CURRENT_YEAR:
                return Map.of("from", DateFunctions.toFirstDayOfYear(now), "to", DateFunctions.toEndDay(now));
            case LAST_YEAR:
                LocalDateTime lastYearStart = DateFunctions.toFirstDayOfYear(now.minusYears(1));
                LocalDateTime lastYearEnd = DateFunctions.toLastDayOfYear(now.minusYears(1));
                return Map.of("from", lastYearStart, "to", lastYearEnd);
            default:
                throw new BadRequestException("Unknown period filter: " + filter);
        }
    }

    /**
     * Determines if the given period filter is considered an advanced filter. Advanced filters include LAST_3_MONTHS, LAST_6_MONTHS, CURRENT_YEAR, and LAST_YEAR.
     * @return true if the period filter is an advanced filter, false otherwise
     */
    public Boolean isAdvancedFilter() {
        return switch (this) {
            case LAST_3_MONTHS, LAST_6_MONTHS, CURRENT_YEAR, LAST_YEAR -> true;
            default -> false;
        };
    }
}
