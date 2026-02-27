package org.xpenbox.enforcement.service.impl;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.xpenbox.common.DateFunctions;
import org.xpenbox.dashboard.dto.PeriodFilter;
import org.xpenbox.enforcement.dto.SnapshotPlanDTO;
import org.xpenbox.enforcement.service.IPlanValidatorService;
import org.xpenbox.enforcement.service.IPlanUsageService;
import org.xpenbox.exception.PlanException;
import org.xpenbox.payment.dto.PlanFeatureResponseDTO;
import org.xpenbox.payment.enums.FeatureCodeEnum;
import org.xpenbox.transaction.dto.TransactionFilterDTO;

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
        validateResourceLimit(
            snapshot,
            FeatureCodeEnum.ACCOUNTS_LIMIT,
            "accounts",
            () -> planUsageService.countUserAccounts(snapshot.userId())
        );
    }

    @Override
    public void validateCanCreateCreditCards(SnapshotPlanDTO snapshot) {
        validateResourceLimit(
            snapshot,
            FeatureCodeEnum.CREDIT_CARDS_LIMIT,
            "credit cards",
            () -> planUsageService.countUserCreditCards(snapshot.userId())
        );
    }

    @Override
    public void validateCanCreateCategories(SnapshotPlanDTO snapshot) {
        validateResourceLimit(
            snapshot,
            FeatureCodeEnum.CATEGORIES_LIMIT,
            "categories",
            () -> planUsageService.countUserCategories(snapshot.userId())
        );
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
        validateResourceLimit(
            snapshot,
            FeatureCodeEnum.TRANSACTIONS_LIMIT,
            "transactions",
            () -> planUsageService.countUserTransactionsInCurrentPeriod(snapshot.userId())
        );
    }
    
    @Override
    public TransactionFilterDTO validateTransactionFilterDTO(SnapshotPlanDTO snapshot, TransactionFilterDTO filter) {
        PlanFeatureResponseDTO historyFeature = snapshot.plan().features().get(FeatureCodeEnum.TRANSACTION_HISTORY_MONTHS);

        if (historyFeature.limitValue() == null) {
            LOG.debugf("User %d has access to advanced transaction filters and no limit on transaction history months", snapshot.userId());
            return filter;
        }

        return adjustDateRangeToHistoryLimit(snapshot.userId(), filter, historyFeature);
    }

    /**
     * Adjusts the date range to respect the transaction history limit defined in the user's plan.
     * @param userId The ID of the user
     * @param filter The original transaction filter DTO
     * @param historyFeature The plan feature response DTO for transaction history months
     * @return The adjusted transaction filter DTO with date range respecting the history limit
     */
    private TransactionFilterDTO adjustDateRangeToHistoryLimit(
            Long userId,
            TransactionFilterDTO filter,
            PlanFeatureResponseDTO historyFeature) {
        
        if (historyFeature.limitValue() == null) {
            return filter;
        }

        Long monthsLimit = historyFeature.limitValue();
        LocalDateTime now = DateFunctions.toStartDay(DateFunctions.currentLocalDateTime().plusDays(1));
        LocalDateTime limitDate = now.minusMonths(monthsLimit).minusDays(1);
        
        Long adjustedFrom = adjustFromDate(userId, filter.transactionDateTimestampFrom(), limitDate, monthsLimit);
        Long adjustedTo = adjustToDate(userId, filter.transactionDateTimestampTo(), limitDate, now, monthsLimit);

        return new TransactionFilterDTO(
            filter.resourceCode(),
            filter.transactionType(),
            filter.description(),
            adjustedFrom,
            adjustedTo,
            filter.categoryResourceCode(),
            filter.incomeResourceCode(),
            filter.accountResourceCode(),
            filter.creditCardResourceCode(),
            filter.pageNumber(),
            filter.pageSize()
        );
    }

    /**
     * Adjusts the "from" date to ensure it does not exceed the transaction history limit.
     * @param userId The ID of the user
     * @param fromTimestamp The original "from" timestamp
     * @param limitDate The earliest allowed date based on the user's plan
     * @param monthsLimit The number of months allowed by the user's plan
     * @return The adjusted "from" timestamp
     */
    private Long adjustFromDate(Long userId, Long fromTimestamp, LocalDateTime limitDate, Long monthsLimit) {
        LocalDateTime from = DateFunctions.convertToLocalDateTime(fromTimestamp);
        
        if (from == null || from.isBefore(limitDate)) {
            LOG.debugf("User %d has a transaction history limit of %d months, adjusting transactionDateTimestampFrom to %s", userId, monthsLimit, limitDate);
            return DateFunctions.convertToTimestamp(limitDate);
        }
        
        return fromTimestamp;
    }

    /**
     * Adjusts the "to" date to ensure it does not exceed the current date and respects the transaction history limit.
     * @param userId The ID of the user
     * @param toTimestamp The original "to" timestamp
     * @param limitDate The earliest allowed date based on the user's plan
     * @param now The current date and time
     * @param monthsLimit The number of months allowed by the user's plan
     * @return The adjusted "to" timestamp

     */
    private Long adjustToDate(Long userId, Long toTimestamp, LocalDateTime limitDate, LocalDateTime now, Long monthsLimit) {
        LocalDateTime to = DateFunctions.convertToLocalDateTime(toTimestamp);
        
        if (to == null || to.isBefore(limitDate)) {
            LOG.debugf("User %d has a transaction history limit of %d months, adjusting transactionDateTimestampTo to %s", userId, monthsLimit, limitDate);
            return DateFunctions.convertToTimestamp(now);
        }
        
        return toTimestamp;
    }

    /**
     * Generic method to validate resource limits based on plan features.
     * 
     * @param snapshot      The user's plan snapshot
     * @param featureCode   The feature code to validate
     * @param resourceName  Human-readable name of the resource (e.g., "accounts", "credit cards")
     * @param usageCounter  Supplier that returns the current usage count
     */
    private void validateResourceLimit(
            SnapshotPlanDTO snapshot,
            FeatureCodeEnum featureCode,
            String resourceName,
            Supplier<Long> usageCounter) {
        
        PlanFeatureResponseDTO feature = snapshot.plan().features().get(featureCode);

        if (!feature.isEnabled()) {
            LOG.debugf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                String.format("Your current plan does not allow creating %s. Please upgrade your plan to access this feature.", resourceName),
                featureCode);
        }

        if (feature.limitValue() == null) {
            LOG.debugf("Feature %s is enabled but has no limit", featureCode);
            return;
        }

        Long limit = feature.limitValue();
        Long currentUsage = usageCounter.get();

        if (currentUsage >= limit) {
            LOG.debugf("User %d has reached the %s limit: %d/%d", snapshot.userId(), resourceName, currentUsage, limit);
            throw new PlanException(
                String.format("You have reached the maximum number of %s allowed by your current plan (%d/%d). Please upgrade your plan to create more %s.", 
                    resourceName, currentUsage, limit, resourceName),
                featureCode,
                limit,
                currentUsage);
        }
        
        LOG.debugf("User %d has not reached the %s limit: %d/%d", snapshot.userId(), resourceName, currentUsage, limit);
    }
}
