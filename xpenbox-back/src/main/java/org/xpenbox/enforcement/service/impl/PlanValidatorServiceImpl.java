package org.xpenbox.enforcement.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.jboss.logging.Logger;
import org.xpenbox.dashboard.dto.PeriodFilter;
import org.xpenbox.enforcement.dto.SnapshotPlanDTO;
import org.xpenbox.enforcement.service.IPlanValidatorService;
import org.xpenbox.enforcement.service.IPlanUsageService;
import org.xpenbox.exception.PlanException;
import org.xpenbox.payment.dto.PlanFeatureResponseDTO;
import org.xpenbox.payment.enums.FeatureCodeEnum;
import org.xpenbox.transaction.dto.TransactionFilterDTO;
import org.xpenbox.transaction.entity.Transaction.TransactionType;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * PlanLimitValidatorServiceImpl is a concrete implementation of the IPlanLimitValidatorService interface. It provides methods to validate whether a user can create more accounts, credit cards, or categories based on their current plan limits. If the user has reached the limit for any of these resources, an exception should be thrown.
 */
@ApplicationScoped
public class PlanValidatorServiceImpl implements IPlanValidatorService {
    private static final Logger LOG = Logger.getLogger(PlanValidatorServiceImpl.class);

    private final IPlanUsageService planUsageService;

    public PlanValidatorServiceImpl(IPlanUsageService planUsageService) {
        this.planUsageService = planUsageService;
    }

    @Override
    public void validateCanCreateAccounts(SnapshotPlanDTO snapshot) {
        FeatureCodeEnum featureCode = FeatureCodeEnum.ACCOUNTS_LIMIT;
        PlanFeatureResponseDTO feature = snapshot.plan().features().get(featureCode);

        if (!feature.isEnabled()) {
            LOG.debugf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating accounts. Please upgrade your plan to access this feature.",
                featureCode);
        }

        if (feature.limitValue() == null) {
            LOG.debugf("Feature %s is enabled but has no limit", featureCode);
            return;
        }

        Long limit = feature.limitValue();
        Long currentUsage = planUsageService.countUserAccounts(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.debugf("User %d has reached the accounts limit: %d/%d", snapshot.userId(), currentUsage, limit);
            throw new PlanException(
                String.format("You have reached the maximum number of accounts allowed by your current plan (%d/%d). Please upgrade your plan to create more accounts.", currentUsage, limit),
                featureCode,
                limit,
                currentUsage);
        }
    }

    @Override
    public void validateCanCreateCreditCards(SnapshotPlanDTO snapshot) {
        FeatureCodeEnum featureCode = FeatureCodeEnum.CREDIT_CARDS_LIMIT;
        PlanFeatureResponseDTO feature = snapshot.plan().features().get(featureCode);

        if (!feature.isEnabled()) {
            LOG.debugf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating credit cards. Please upgrade your plan to access this feature.",
                featureCode);
        }

        Long limit = feature.limitValue();
        Long currentUsage = planUsageService.countUserCreditCards(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.debugf("User %d has reached the credit cards limit: %d/%d", snapshot.userId(), currentUsage, limit);
            throw new PlanException(
                String.format("You have reached the maximum number of credit cards allowed by your current plan (%d/%d). Please upgrade your plan to create more credit cards.", currentUsage, limit),
                featureCode,
                limit,
                currentUsage);
        }
    }

    @Override
    public void validateCanCreateCategories(SnapshotPlanDTO snapshot) {
        FeatureCodeEnum featureCode = FeatureCodeEnum.CATEGORIES_LIMIT;
        PlanFeatureResponseDTO feature = snapshot.plan().features().get(featureCode);

        if (!feature.isEnabled()) {
            LOG.debugf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating categories. Please upgrade your plan to access this feature.",
                featureCode);
        }

        Long limit = feature.limitValue();
        Long currentUsage = planUsageService.countUserCategories(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.debugf("User %d has reached the categories limit: %d/%d", snapshot.userId(), currentUsage, limit);
            throw new PlanException(
                String.format("You have reached the maximum number of categories allowed by your current plan (%d/%d). Please upgrade your plan to create more categories.", currentUsage, limit),
                featureCode,
                limit,
                currentUsage);
        }
    }

    @Override
    public void validateCanUseAdvancedDashboardFilters(SnapshotPlanDTO snapshot, PeriodFilter periodFilter) {
        FeatureCodeEnum featureCode = FeatureCodeEnum.DASHBOARD_ADVANCED_FILTERS;
        PlanFeatureResponseDTO feature = snapshot.plan().features().get(featureCode);
   
        if (!feature.isEnabled() && periodFilter.isAdvancedFilter()) {
            LOG.debugf("User %d is trying to use advanced dashboard filters but the feature is not enabled", snapshot.userId());
            throw new PlanException(
                "Your current plan does not allow using advanced dashboard filters. Please upgrade your plan to access this feature.",
                featureCode);
        }
    }

    @Override
    public void validateCanCreateTransactions(SnapshotPlanDTO snapshot) {
        FeatureCodeEnum featureCode = FeatureCodeEnum.TRANSACTIONS_LIMIT;
        PlanFeatureResponseDTO feature = snapshot.plan().features().get(featureCode);
        
        if(!feature.isEnabled()) {
            LOG.debugf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating transactions. Please upgrade your plan to access this feature.",
                featureCode);
        }

        if (feature.limitValue() == null) {
            LOG.debugf("Feature %s is enabled but has no limit", featureCode);
            return;
        }

        Long limit = feature.limitValue();
        Long currentUsage = planUsageService.countUserTransactionsInCurrentPeriod(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.debugf("User %d has reached the transactions limit: %d/%d", snapshot.userId(), currentUsage, limit);
            throw new PlanException(
                String.format("You have reached the maximum number of transactions allowed by your current plan for the current period (%d/%d). Please upgrade your plan to create more transactions.", currentUsage, limit),
                featureCode,
                limit,
                currentUsage);
        } 
        
        LOG.debugf("User %d has not reached the transactions limit: %d/%d", snapshot.userId(), currentUsage, limit);
    }
    
    @Override
    public TransactionFilterDTO validateTransactionFilterDTO(SnapshotPlanDTO snapshot, TransactionFilterDTO transactionFilterDTO) {
        FeatureCodeEnum transactionHistoryMonths = FeatureCodeEnum.TRANSACTION_HISTORY_MONTHS;
        FeatureCodeEnum advancedTransactionSearch = FeatureCodeEnum.ADVANCED_TRANSACTION_SEARCH;

        PlanFeatureResponseDTO featureTransactionHistoryMonths = snapshot.plan().features().get(transactionHistoryMonths);
        PlanFeatureResponseDTO featureAdvancedTransactionSearch = snapshot.plan().features().get(advancedTransactionSearch);

        if (featureTransactionHistoryMonths.limitValue() == null && featureAdvancedTransactionSearch.isEnabled()) {
            LOG.debugf("User %d has access to advanced transaction filters and no limit on transaction history months", snapshot.userId());
            return transactionFilterDTO;
        }

        TransactionType transactionType = transactionFilterDTO.transactionType();
        String description = transactionFilterDTO.description();
        Long transactionDateTimestampFrom = transactionFilterDTO.transactionDateTimestampFrom();
        Long transactionDateTimestampTo = transactionFilterDTO.transactionDateTimestampTo();
        String categoryResourceCode = transactionFilterDTO.categoryResourceCode();

        if (!featureAdvancedTransactionSearch.isEnabled()) {
            LOG.debugf("User %d does not have access to advanced transaction filters, removing advanced filters from TransactionFilterDTO", snapshot.userId());
            transactionType = null;
            description = null;
            categoryResourceCode = null;
        }

        if (featureTransactionHistoryMonths.limitValue() != null) {
            Long monthsLimit = featureTransactionHistoryMonths.limitValue();
            LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime limitDate = now.minusMonths(monthsLimit);
            LocalDateTime from = transactionFilterDTO.transactionDateTimestampFrom() != null ? Instant.ofEpochMilli(transactionFilterDTO.transactionDateTimestampFrom()).atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
            LocalDateTime to = transactionFilterDTO.transactionDateTimestampTo() != null ? Instant.ofEpochMilli(transactionFilterDTO.transactionDateTimestampTo()).atZone(ZoneId.systemDefault()).toLocalDateTime() : null;

            if (from == null || from.isBefore(limitDate)) {
                LOG.debugf("User %d has a transaction history limit of %d months, adjusting transactionDateTimestampFrom to %s", snapshot.userId(), monthsLimit, limitDate);
                transactionDateTimestampFrom = limitDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }

            if (to == null || to.isBefore(limitDate)) {
                LOG.debugf("User %d has a transaction history limit of %d months, adjusting transactionDateTimestampTo to %s", snapshot.userId(), monthsLimit, limitDate);
                transactionDateTimestampTo = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            
        }

        return new TransactionFilterDTO(
            transactionFilterDTO.resourceCode(),
            transactionType,
            description,
            transactionDateTimestampFrom,
            transactionDateTimestampTo,
            categoryResourceCode,
            transactionFilterDTO.incomeResourceCode(),
            transactionFilterDTO.accountResourceCode(),
            transactionFilterDTO.creditCardResourceCode(),
            transactionFilterDTO.pageNumber(),
            transactionFilterDTO.pageSize()
        );
    }
}
