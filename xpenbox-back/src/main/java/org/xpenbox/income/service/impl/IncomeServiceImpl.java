package org.xpenbox.income.service.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.jboss.logging.Logger;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.income.dto.IncomeCreateDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.income.dto.IncomeUpdateDTO;
import org.xpenbox.income.entity.Income;
import org.xpenbox.income.mapper.IncomeMapper;
import org.xpenbox.income.repository.IncomeRepository;
import org.xpenbox.income.service.IIncomeService;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.transaction.repository.TransactionRepository;
import org.xpenbox.transaction.service.ITransactionService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * IncomeServiceImpl provides the implementation of income related operations.
 */
@ApplicationScoped
public class IncomeServiceImpl extends GenericServiceImpl<Income, IncomeCreateDTO, IncomeUpdateDTO, IncomeResponseDTO> implements IIncomeService {
    private static final Logger LOG = Logger.getLogger(IncomeServiceImpl.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ITransactionService transactionService;
    private final IncomeRepository incomeRepository;
    private final IncomeMapper incomeMapper;

    public IncomeServiceImpl(
        UserRepository userRepository,
        TransactionRepository transactionRepository,
        ITransactionService transactionService,
        IncomeRepository incomeRepository,
        IncomeMapper incomeMapper
    ) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
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
    
    /**
     * Create a new income and an associated transaction if accountResourceCode is provided
     * @param incomeCreateDTO the income creation data transfer object
     * @param userEmail the email of the user creating the income
     * @return the created income response DTO
     */
    @Override
    public IncomeResponseDTO create(IncomeCreateDTO incomeCreateDTO, String userEmail) {
        LOG.infof("Creating income for user email: %s", userEmail);
        IncomeResponseDTO incomeResponseDTO = super.create(incomeCreateDTO, userEmail);

        if (incomeCreateDTO.accountResourceCode() != null && !incomeCreateDTO.accountResourceCode().isEmpty()) {
            TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO(
                TransactionType.INCOME,
                "Auto-generated allocation for income: " + incomeCreateDTO.concept(),
                incomeCreateDTO.totalAmount(),
                null,
                null,
                incomeCreateDTO.incomeDateTimestamp(),
                null,
                incomeResponseDTO.resourceCode(),
                incomeCreateDTO.accountResourceCode(),
                null,
                null
            );
    
            transactionService.create(transactionCreateDTO, userEmail);
        }

        return incomeResponseDTO;
    }

    /**
     * Update an existing income
     * @param resourceCode the resource code of the income to update
     * @param incomeUpdateDTO the income update data transfer object
     * @param userEmail the email of the user performing the update
     * @return the updated income response DTO
     */
    @Override
    public IncomeResponseDTO update(String resourceCode, IncomeUpdateDTO incomeUpdateDTO, String userEmail) {
        LOG.infof("Updating income with resource code %s for user email: %s", resourceCode, userEmail);
        
        User user = validateAndGetUser(userEmail);

        Income existingIncome = incomeRepository.findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("Income not found with resource code: %s for user email: %s", resourceCode, userEmail);
                throw new ResourceNotFoundException("Income not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });
            
        BigDecimal totalAssignedToTransactions = calculateTotalIncomeAsignedToTransactions(existingIncome.id, user.id);
        if (incomeUpdateDTO.totalAmount().compareTo(totalAssignedToTransactions) < 0) {
            LOG.errorf("Updated income amount %s is less than total assigned to transactions %s for income resource code: %s and user email: %s", 
                incomeUpdateDTO.totalAmount(), totalAssignedToTransactions, resourceCode, userEmail);
            throw new BadRequestException("Updated income amount cannot be less than total amount assigned to transactions: " + totalAssignedToTransactions);
        }

        boolean updated = incomeMapper.updateEntity(incomeUpdateDTO, existingIncome);

        if (updated) {
            incomeRepository.persist(existingIncome);
            LOG.infof("Income updated with resource code: %s", resourceCode);
        } else {
            LOG.infof("No changes detected for income with resource code: %s", resourceCode);
        }

        return super.update(resourceCode, incomeUpdateDTO, userEmail);
    }

    /**
     * Get all incomes for a user
     * @param userEmail the email of the user
     * @return a list of income response DTOs
     */
    @Override
    public List<IncomeResponseDTO> getAll(String userEmail) {
        LOG.infof("Retrieving all incomes for user email: %s", userEmail);
        User user = validateAndGetUser(userEmail);

        List<Income> incomes = incomeRepository.findAllByUserId(user.id);
        LOG.infof("Found %d incomes for user email: %s", incomes.size(), userEmail);
        
        List<Long> incomeIds = incomes.stream()
            .map(income -> income.id)
            .toList();
        Map<Long, BigDecimal> allocatedAmounts = transactionRepository.findAssignedAmountByIncomeIdsAndUserIdAndTransactionType(incomeIds, user.id, TransactionType.INCOME);

        LOG.infof("Calculated allocated amounts for incomes: %s", allocatedAmounts);
        return incomeMapper.toDTOListAllocated(incomes, allocatedAmounts);
    }

    /**
     * Get an income by its resource code, including allocated amount.
     * @param resourceCode the resource code of the income
     * @param userEmail the email of the user
     * @return the income response DTO
     */
    @Override
    public IncomeResponseDTO getByResourceCode(String resourceCode, String userEmail) {
        LOG.infof("Retrieving income with resource code %s for user email: %s", resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);

        Income existingIncome = incomeRepository.findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("Income not found with resource code: %s for user email: %s", resourceCode, userEmail);
                throw new ResourceNotFoundException("Income not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });

        BigDecimal allocatedAmount = calculateTotalIncomeAsignedToTransactions(existingIncome.id, user.id);
        LOG.infof("Calculated allocated amount %s for income with resource code: %s", allocatedAmount, resourceCode);

        IncomeResponseDTO incomeResponseDTO = incomeMapper.toDTOAllocated(existingIncome, allocatedAmount);
        return incomeResponseDTO;
    }

    @Override
    public List<IncomeResponseDTO> filterIncomesByDateRange(String userEmail, Long startDateTimestamp, Long endDateTimestamp) {
        LOG.infof("Filtering incomes for user email: %s between timestamps %d and %d", userEmail, startDateTimestamp, endDateTimestamp);
        User user = validateAndGetUser(userEmail);

        
        LocalDate startDate = Instant.ofEpochMilli(startDateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(endDateTimestamp).atZone(ZoneId.systemDefault()).toLocalDate();

        if (endDate.compareTo(startDate) < 1) {
            LOG.errorf("Invalid date range: start date %s is after end date %s for user email: %s", startDate, endDate, userEmail);
            throw new BadRequestException("End date must be after start date.");
        } else if (startDate.until(endDate, ChronoUnit.DAYS) > 365) {
            LOG.errorf("Date range exceeds one year: start date %s to end date %s for user email: %s", startDate, endDate, userEmail);
            throw new BadRequestException("Date range cannot exceed one year.");
        }

        List<Income> incomes = incomeRepository.findByUserIdAndDateRange(user.id, startDate, endDate);
        LOG.infof("Found %d incomes for user email: %s in date range", incomes.size(), userEmail);

        List<Long> incomeIds = incomes.stream()
            .map(income -> income.id)
            .toList();
        
        Map<Long, BigDecimal> allocatedAmounts = transactionRepository.findAssignedAmountByIncomeIdsAndUserIdAndTransactionType(incomeIds, user.id, TransactionType.INCOME);

        return incomeMapper.toDTOListAllocated(incomes, allocatedAmounts);
    }

    /**
     * Calculate the total income assigned to transactions for a given income and user.
     * @param incomeId the ID of the income
     * @param userId the ID of the user
     * @return the total income assigned to transactions
     */
    private BigDecimal calculateTotalIncomeAsignedToTransactions(Long incomeId, Long userId) {
        LOG.debugf("Calculating total income assigned to transactions for incomeId: %d and userId: %d", incomeId, userId);
        List<Transaction> transactions = transactionRepository.findByIncomeIdAndUserIdAndType(incomeId, userId, TransactionType.INCOME);
        BigDecimal total = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LOG.debugf("Total income assigned to transactions: %s", total);
        return total;
    }
}
