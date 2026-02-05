package org.xpenbox.income.service;

import java.util.List;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.dto.IncomeUpdateDTO;
import org.xpenbox.income.entity.Income;

/**
 * Service interface for Income entity operations.
 */
public interface IIncomeService extends IGenericService<Income, IncomeCreateDTO, IncomeUpdateDTO, IncomeResponseDTO> {
    
    /**
     * Filter incomes by user email and date range.
     * @param userEmail the email of the user
     * @param startDateTimestamp the start date timestamp in milliseconds
     * @param endDateTimestamp the end date timestamp in milliseconds
     * @return a list of income response DTOs matching the criteria
     */
    List<IncomeResponseDTO> filterIncomesByDateRange(String userEmail, Long startDateTimestamp, Long endDateTimestamp);

    /**
     * Delete an income by resource code and user email.
     * @param resourceCode the resource code of the income
     * @param userEmail the email of the user
     */
    void delete(String resourceCode, String userEmail);
}
