package org.xpenbox.enforcement.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.enforcement.service.IPlanUsageService;

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

    public PlanUsageServiceImpl(
        AccountRepository accountRepository, 
        CreditCardRepository creditCardRepository, 
        CategoryRepository categoryRepository
    ) {
        this.accountRepository = accountRepository;
        this.creditCardRepository = creditCardRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public long countUserAccounts(Long userId) {
        LOG.debugf("Counting user accounts for userId: %d", userId);
        return accountRepository.countAllByUserIdAndStateTrue(userId);
    }

    @Override
    public long countUserCreditCards(Long userId) {
        LOG.debugf("Counting user credit cards for userId: %d", userId);
        return creditCardRepository.countAllByUserIdAndStateTrue(userId);
    }

    @Override
    public long countUserCategories(Long userId) {
        LOG.debugf("Counting user categories for userId: %d", userId);
        return categoryRepository.countAllByUserId(userId);
    }

}
