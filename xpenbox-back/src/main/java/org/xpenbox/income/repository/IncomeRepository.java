package org.xpenbox.income.repository;

import java.time.LocalDate;
import java.util.List;

import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.income.entity.Income;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for Income entity operations.
 */
@ApplicationScoped
public class IncomeRepository extends GenericRepository<Income> {
 
    /**
     * Find incomes by user ID within a specified date range.
     * @param userId the ID of the user
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of Income entities matching the criteria
     */
    public List<Income> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return find(
            "user.id = :userId AND incomeDate >= :startDate AND incomeDate <= :endDate order by incomeDate DESC",
            Parameters.with("userId", userId)
                      .and("startDate", startDate.atStartOfDay())
                      .and("endDate", endDate.atTime(23, 59, 59))
        ).list();
    }
}
