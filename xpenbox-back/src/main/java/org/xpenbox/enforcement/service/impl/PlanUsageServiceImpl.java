package org.xpenbox.enforcement.service.impl;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.common.DateConvertir;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.enforcement.service.IPlanUsageService;
import org.xpenbox.transaction.repository.TransactionRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * PlanUsageServiceImpl is a concrete implementation of the IPlanUsageService interface. It provides methods to manage plan usage for users, including counting user accounts, credit cards, and categories based on user ID.
 */
@ApplicationScoped
public class PlanUsageServiceImpl implements IPlanUsageService {
    private static final Logger LOG = Logger.getLogger(PlanUsageServiceImpl.class);
    
    private final AccountRepository accountRepository;
    private final CreditCardRepository creditCardRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public PlanUsageServiceImpl(
        AccountRepository accountRepository, 
        CreditCardRepository creditCardRepository, 
        CategoryRepository categoryRepository,
        TransactionRepository transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.creditCardRepository = creditCardRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Long countUserAccounts(Long userId) {
        LOG.debugf("Counting user accounts for userId: %d", userId);
        return accountRepository.countAllByUserIdAndStateTrue(userId);
    }

    @Override
    public Long countUserCreditCards(Long userId) {
        LOG.debugf("Counting user credit cards for userId: %d", userId);
        return creditCardRepository.countAllByUserIdAndStateTrue(userId);
    }

    @Override
    public Long countUserCategories(Long userId) {
        LOG.debugf("Counting user categories for userId: %d", userId);
        return categoryRepository.countAllByUserId(userId);
    }

    @Override
    public Long countUserTransactionsInCurrentPeriod(Long userId) {
        LOG.debugf("Counting user transactions in current period for userId: %d", userId);
        LocalDateTime currentDate = DateConvertir.currentLocalDateTime();
        LocalDateTime firstDay = DateConvertir.toFirstDayOfMonth(currentDate); 
        LocalDateTime lastDay = DateConvertir.toLastDayOfMonth(currentDate);
        return transactionRepository.countByUserIdAndPeriodRange(userId, firstDay, lastDay);
    }

}
