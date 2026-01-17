package org.xpenbox.income.service.impl;

import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.dto.IncomeUpdateDTO;
import org.xpenbox.income.entity.Income;
import org.xpenbox.income.mapper.IncomeMapper;
import org.xpenbox.income.repository.IncomeRepository;
import org.xpenbox.income.service.IIncomeService;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * IncomeServiceImpl provides the implementation of income related operations.
 */
@ApplicationScoped
public class IncomeServiceImpl extends GenericServiceImpl<Income, IncomeCreateDTO, IncomeUpdateDTO, IncomeResponseDTO> implements IIncomeService {

    private final UserRepository userRepository;
    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;

    public IncomeServiceImpl(
        UserRepository userRepository,
        IncomeRepository incomeRepository,
        IncomeMapper incomeMapper
    ) {
        this.userRepository = userRepository;
        this.incomeRepository = incomeRepository;
        this.incomeMapper = incomeMapper;
    }

    @Override
    protected String getEntityName() {
        return "Income";
    }

    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected IncomeRepository getGenericRepository() {
        return incomeRepository;
    }

    @Override
    protected IncomeMapper getGenericMapper() {
        return incomeMapper;
    }
    
}
