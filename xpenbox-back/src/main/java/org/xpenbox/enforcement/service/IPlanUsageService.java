package org.xpenbox.enforcement.service;

/**
 * IPlanUsageService is an interface that defines the contract for managing plan usage for users. It provides methods to count user accounts, credit cards, and categories based on user ID.
 */
public interface IPlanUsageService {

    /**
     * Counts the number of user accounts, credit cards, and categories for a given user ID.
     * @param userId the ID of the user for whom the plan usage is to be counted
     * @return a long representing the count of user accounts, credit cards, and categories for the specified user
     */
    Long countUserAccounts(Long userId);

    /**
     * Counts the number of credit cards and categories for a given user ID.
     * @param userId the ID of the user for whom the plan usage is to be counted
     * @return a long representing the count of credit cards and categories for the specified user
     */
    Long countUserCreditCards(Long userId);

    /**
     * Counts the number of categories for a given user ID.
     * @param userId the ID of the user for whom the plan usage is to be counted
     * @return a long representing the count of categories for the specified user
     */
    Long countUserCategories(Long userId);

    /**
     * Counts the number of transactions for a given user ID in the current period.
     * @param userId the ID of the user for whom the plan usage is to be counted
     * @return a long representing the count of transactions for the specified user
     */
    Long countUserTransactionsInCurrentPeriod(Long userId);
}
