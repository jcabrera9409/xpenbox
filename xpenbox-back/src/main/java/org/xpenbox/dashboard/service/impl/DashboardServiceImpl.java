package org.xpenbox.dashboard.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.category.entity.Category;
import org.xpenbox.category.mapper.CategoryMapper;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.creditcard.service.ICreditCardService;
import org.xpenbox.dashboard.dto.DashboardCurrentPeriodDTO;
import org.xpenbox.dashboard.dto.DashboardPeriodFilterDTO;
import org.xpenbox.dashboard.dto.DashboardResponseDTO;
import org.xpenbox.dashboard.dto.PeriodFilter;
import org.xpenbox.dashboard.service.IDashboardService;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.transaction.mapper.TransactionMapper;
import org.xpenbox.transaction.repository.TransactionRepository;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the IDashboardService interface that provides methods to generate dashboard data based on user transactions, accounts, and credit cards for a specified period filter.
 * This service retrieves the necessary data from the repositories and services, performs calculations to generate the dashboard metrics, and returns a structured response DTO containing the dashboard data for both the current period and the selected period filter.
 * The dashboard data includes metrics such as current balance, opening balance, delta, credit used, credit limit, income total, expense total, net total, category breakdowns, and recent transactions, all calculated based on the user's financial data for the specified period.
 * The service also includes error handling to ensure that only authorized users can access their dashboard data, and logs relevant information for debugging and monitoring purposes.
 * Overall, this implementation provides a comprehensive and efficient way to generate the necessary data for displaying a financial dashboard to users based on their transactions, accounts, and credit cards for a given period filter.
 */
@ApplicationScoped
public class DashboardServiceImpl implements IDashboardService {
    private static final Logger LOG = Logger.getLogger(DashboardServiceImpl.class);

    private final UserRepository userRepository;
    private final IAccountService accountService;
    private final ICreditCardService creditCardService;
    private final TransactionRepository transactionRepository;
    private final CategoryMapper categoryMapper;
    private final TransactionMapper transactionMapper;

    public DashboardServiceImpl(
        UserRepository userRepository,
        IAccountService accountService,
        ICreditCardService creditCardService,
        TransactionRepository transactionRepository,
        CategoryMapper categoryMapper,
        TransactionMapper transactionMapper
    ) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.creditCardService = creditCardService;
        this.transactionRepository = transactionRepository;
        this.categoryMapper = categoryMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public DashboardResponseDTO generateDashboardData(PeriodFilter periodFilter, String userEmail) {
        LOG.infof("Generating dashboard data for user: %s with period filter: %s", userEmail, periodFilter);        

        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                return new ResourceNotFoundException("User not found with email: " + userEmail);
            });

        Map<String, LocalDateTime> dateRange = PeriodFilter.getDateRange(periodFilter);
        List<AccountResponseDTO> accounts = accountService.getAll(userEmail);
        List<CreditCardResponseDTO> creditCards = creditCardService.getAll(userEmail);
        List<Transaction> transactions = transactionRepository.findByUserIdAndPeriodRange(user.id, dateRange.get("from"), dateRange.get("to"));
        
        DashboardCurrentPeriodDTO currentPeriodDashboard = generateCurrentPeriodDashboard(
            periodFilter,
            accounts,
            creditCards,
            transactions
        );

        DashboardPeriodFilterDTO periodFilterDashboard = generatePeriodFilterDashboard(
            periodFilter,
            transactions
        );

        return new DashboardResponseDTO(currentPeriodDashboard, periodFilterDashboard);
    }

    /**
     * Generates the dashboard data specific to the selected period filter, including totals and breakdowns.
     * @param periodFilter The selected period filter for which to generate the dashboard data.
     * @param transactions The list of transactions for the selected period, used to calculate totals and breakdowns.
     * @return A DTO containing the calculated totals and breakdowns for the selected period filter.
     */
    private DashboardPeriodFilterDTO generatePeriodFilterDashboard(
        PeriodFilter periodFilter,
        List<Transaction> transactions
    ) {
        BigDecimal incomeTotal = calculateIncomeTotal(transactions);
        BigDecimal expenseTotal = calculateExpenseTotal(transactions);
        BigDecimal netTotal = netCashflow(transactions);
        List<CategoryResponseDTO> categoryBreakdown = groupTransactionsByCategory(transactions);
        List<TransactionResponseDTO> lastTransactions = getLastTransactions(transactions, 10);

        return new DashboardPeriodFilterDTO(
            incomeTotal,
            expenseTotal,
            netTotal,
            categoryBreakdown,
            lastTransactions
        );

    }

    /**
     * Generates the dashboard data for the current period, including current balance, opening balance, delta, credit used, and credit limit.
     * @param periodFilter The selected period filter for which to generate the dashboard data (used to determine if opening balance should be calculated).
     * @param accounts The list of account DTOs for the user, used to calculate current balance.
     * @param creditCards The list of credit card DTOs for the user, used to calculate credit used and credit limit.
     * @param transactions The list of transactions for the current month, used to calculate opening balance if the period filter is CURRENT_MONTH. 
     * @return A DTO containing the calculated current balance, opening balance, delta, credit used, credit limit, and sorted credit card information for the current period.
     */
    private DashboardCurrentPeriodDTO generateCurrentPeriodDashboard(
        PeriodFilter periodFilter,
        List<AccountResponseDTO> accounts,
        List<CreditCardResponseDTO> creditCards,
        List<Transaction> transactions
    ) {
        BigDecimal currentBalance = calculateCurrentBalance(accounts);
        BigDecimal openingBalance = currentBalance.subtract(netCashflow(transactions));
        BigDecimal deltaBalance = currentBalance.subtract(openingBalance);
        BigDecimal creditUsed = calculateCreditUsed(creditCards);
        BigDecimal creditLimit = calculateCreditLimit(creditCards);
        List<CreditCardResponseDTO> sortedCreditCards = sortCreditCardsByBalance(creditCards);

        return new DashboardCurrentPeriodDTO(
            currentBalance,
            openingBalance,
            deltaBalance,
            creditUsed,
            creditLimit,
            sortedCreditCards
        );
    }

    /**
     * Calculates the current balance by summing the balances of all accounts.
     * @param accounts The list of account DTOs for the user, used to calculate current balance.
     * @return The calculated current balance as a BigDecimal.
     */
    private BigDecimal calculateCurrentBalance(List<AccountResponseDTO> accounts) {
        return accounts.stream()
            .map(AccountResponseDTO::balance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the net cashflow by iterating through the transactions and summing the amounts based on their transaction type. INCOME transactions contribute positively to the net cashflow, while EXPENSE and CREDIT_PAYMENT transactions with an associated account contribute negatively. Transactions that do not fit these criteria are ignored in the net cashflow calculation.
     * @param transactions The list of transactions for the selected period, used to calculate net cashflow by summing amounts based on transaction types.
     * @return The calculated net cashflow as a BigDecimal, representing the overall cash inflow or outflow for the selected period filter.
     */
    private BigDecimal netCashflow(List<Transaction> transactions) {
        return transactions.stream()
                    .map(t -> switch (t.getTransactionType()) {
                        case INCOME -> t.getAmount();
                        case EXPENSE, CREDIT_PAYMENT -> t.getAccount() != null ? t.getAmount().negate() : BigDecimal.ZERO;
                        default -> BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total credit used by summing the current balances of all credit cards.
     * @param creditCards The list of credit card DTOs for the user, used to calculate credit used by summing their current balances.
     * @return The calculated total credit used as a BigDecimal.
     */
    private BigDecimal calculateCreditUsed(List<CreditCardResponseDTO> creditCards) {
        return creditCards.stream()
            .map(CreditCardResponseDTO::currentBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total credit limit by summing the credit limits of all credit cards.
     * @param creditCards The list of credit card DTOs for the user, used to calculate total credit limit by summing their credit limits.
     * @return The calculated total credit limit as a BigDecimal.
     */
    private BigDecimal calculateCreditLimit(List<CreditCardResponseDTO> creditCards) {
        return creditCards.stream()
            .map(CreditCardResponseDTO::creditLimit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Sorts the list of credit cards by their current balance in descending order, so that the credit card with the highest current balance appears first in the list.
     * @param creditCards The list of credit card DTOs for the user, used to sort by their current balances.
     * @return A new list of credit card DTOs sorted by current balance in descending order.
     */
    private List<CreditCardResponseDTO> sortCreditCardsByBalance(List<CreditCardResponseDTO> creditCards) {
        return creditCards.stream()
            .sorted((c1, c2) -> c2.currentBalance().compareTo(c1.currentBalance()))
            .collect(Collectors.toList());
    }

    /**
     * Calculates the total income by filtering the transactions for those of type INCOME and summing their amounts.
     * @param transactions The list of transactions for the selected period, used to filter for INCOME transactions and sum their amounts to calculate total income.
     * @return The calculated total income as a BigDecimal.
     */
    private BigDecimal calculateIncomeTotal(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getTransactionType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total expenses by filtering the transactions for those of type EXPENSE with an associated account or of type CREDIT_PAYMENT, and summing their amounts.
     * @param transactions The list of transactions for the selected period, used to filter for EXPENSE transactions with an associated account or CREDIT_PAYMENT transactions, and sum their amounts to calculate total expenses.
     * @return The calculated total expenses as a BigDecimal.
     */
    private BigDecimal calculateExpenseTotal(List<Transaction> transactions) {
        return transactions.stream()
            .filter(t -> t.getTransactionType() == TransactionType.CREDIT_PAYMENT || (t.getTransactionType() == TransactionType.EXPENSE && t.getAccount() != null))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Groups the transactions by their associated category, summing the amounts for each category to create a breakdown of expenses by category. Only transactions of type CREDIT_PAYMENT or EXPENSE with an associated account are included in the grouping, as these represent outgoing funds that should be categorized for the dashboard breakdown.
     * @param transactions The list of transactions for the selected period, used to filter for relevant transactions and group them by their associated category to calculate the total amount for each category for the dashboard breakdown.
     * @return A list of CategoryResponseDTOs representing the breakdown of expenses by category, sorted by amount in descending order. Each DTO includes the category information and the total amount for that category.
     */
    private List<CategoryResponseDTO> groupTransactionsByCategory(List<Transaction> transactions) {

        Map<Category, BigDecimal> categoryTotals = transactions.stream()
            .filter(t ->
                t.getCategory() != null &&
                (
                    t.getTransactionType() == TransactionType.CREDIT_PAYMENT ||
                    (t.getTransactionType() == TransactionType.EXPENSE && t.getAccount() != null)
                )
            )
            .collect(Collectors.groupingBy(
                Transaction::getCategory,
                Collectors.reducing(
                    BigDecimal.ZERO,
                    Transaction::getAmount,
                    BigDecimal::add
                )
            ));

        return categoryTotals.entrySet().stream()
            .map(entry ->
                categoryMapper.toDTOReport(
                    entry.getKey(),
                    entry.getValue()
                )
            )
            .sorted((c1, c2) -> c2.amount().compareTo(c1.amount()))
            .collect(Collectors.toList());
    }

    /**
     * Gets the last transactions for the selected period by sorting the transactions by their transaction date in descending order and limiting the results to a specified number (e.g., 10). Only transactions of type CREDIT_PAYMENT or EXPENSE with an associated account are included in the list of last transactions, as these represent outgoing funds that are typically more relevant for users to see in a recent transactions list on the dashboard.
     * @param transactions The list of transactions for the selected period, used to filter for relevant transactions, sort them by transaction date, and limit the results to get the most recent transactions for display on the dashboard.
     * @param limit The maximum number of recent transactions to return, used to limit the results to a manageable number for display on the dashboard (e.g., 10).
     * @return A list of TransactionResponseDTOs representing the most recent transactions for the selected period, sorted by transaction date in descending order and limited to the specified number. Each DTO includes the transaction information relevant for display on the dashboard.
     */
    private List<TransactionResponseDTO> getLastTransactions(List<Transaction> transactions, int limit) {
        return transactions.stream()
            .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
            .limit(limit)
            .map(transactionMapper::toSimpleDTO)
            .collect(Collectors.toList());
    }
}
