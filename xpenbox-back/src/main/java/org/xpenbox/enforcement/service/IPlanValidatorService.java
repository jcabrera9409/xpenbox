package org.xpenbox.enforcement.service;

import org.xpenbox.enforcement.dto.SnapshotPlanDTO;

public interface IPlanValidatorService {
    
    /**
     * Validates whether the user can create more accounts based on their current plan limits. If the user has reached the limit for creating accounts, an exception should be thrown.
     * @param snapshot the SnapshotPlanDTO containing the current plan information for the user
     */
    void validateCanCreateAccounts(SnapshotPlanDTO snapshot);

    /**
     * Validates whether the user can create more credit cards based on their current plan limits. If the user has reached the limit for creating credit cards, an exception should be thrown.
     * @param snapshot the SnapshotPlanDTO containing the current plan information for the user
     */
    void validateCanCreateCreditCards(SnapshotPlanDTO snapshot);

    /**
     * Validates whether the user can create more categories based on their current plan limits. If the user has reached the limit for creating categories, an exception should be thrown.
     * @param snapshot the SnapshotPlanDTO containing the current plan information for the user
     */
    void validateCanCreateCategories(SnapshotPlanDTO snapshot);
}
