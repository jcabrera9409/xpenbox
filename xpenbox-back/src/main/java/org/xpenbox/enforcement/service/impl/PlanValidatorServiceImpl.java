package org.xpenbox.enforcement.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.enforcement.dto.SnapshotPlanDTO;
import org.xpenbox.enforcement.service.IPlanValidatorService;
import org.xpenbox.enforcement.service.IPlanUsageService;
import org.xpenbox.exception.PlanException;
import org.xpenbox.payment.dto.PlanFeatureResponseDTO;
import org.xpenbox.payment.enums.FeatureCodeEnum;

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
            LOG.warnf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating accounts. Please upgrade your plan to access this feature.",
                featureCode);
        }

        long limit = feature.limitValue();
        long currentUsage = planUsageService.countUserAccounts(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.warnf("User %d has reached the accounts limit: %d/%d", snapshot.userId(), currentUsage, limit);
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
            LOG.warnf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating credit cards. Please upgrade your plan to access this feature.",
                featureCode);
        }

        long limit = feature.limitValue();
        long currentUsage = planUsageService.countUserCreditCards(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.warnf("User %d has reached the credit cards limit: %d/%d", snapshot.userId(), currentUsage, limit);
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
            LOG.warnf("Feature %s is not enabled", featureCode);
            throw new PlanException(
                "Your current plan does not allow creating categories. Please upgrade your plan to access this feature.",
                featureCode);
        }

        long limit = feature.limitValue();
        long currentUsage = planUsageService.countUserCategories(snapshot.userId());

        if (currentUsage >= limit) {
            LOG.warnf("User %d has reached the categories limit: %d/%d", snapshot.userId(), currentUsage, limit);
            throw new PlanException(
                String.format("You have reached the maximum number of categories allowed by your current plan (%d/%d). Please upgrade your plan to create more categories.", currentUsage, limit),
                featureCode,
                limit,
                currentUsage);
        }
    }
    
}
