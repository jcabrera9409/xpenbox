package org.xpenbox.income.service;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.dto.IncomeUpdateDTO;
import org.xpenbox.income.entity.Income;

/**
 * Service interface for Income entity operations.
 */
public interface IIncomeService extends IGenericService<Income, IncomeCreateDTO, IncomeUpdateDTO, IncomeResponseDTO> {
    
}
