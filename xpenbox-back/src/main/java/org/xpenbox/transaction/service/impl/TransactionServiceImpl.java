package org.xpenbox.transaction.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.jboss.logging.Logger;
import org.xpenbox.account.entity.Account;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.category.entity.Category;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.common.dto.APIPageableDTO;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.creditcard.service.ICreditCardService;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.income.entity.Income;
import org.xpenbox.income.repository.IncomeRepository;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionFilterDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.dto.TransactionUpdateDTO;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.transaction.mapper.TransactionMapper;
import org.xpenbox.transaction.repository.TransactionRepository;
import org.xpenbox.transaction.service.ITransactionService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service implementation for managing Transactions.
 */
@ApplicationScoped
public class TransactionServiceImpl extends GenericServiceImpl<Transaction, TransactionCreateDTO, TransactionUpdateDTO, TransactionResponseDTO> implements ITransactionService {
    private static final Logger LOG = Logger.getLogger(TransactionServiceImpl.class);

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final AccountRepository accountRepository;
    private final IAccountService accountService;
    private final CreditCardRepository creditCardRepository;
    private final ICreditCardService creditCardService;

    public TransactionServiceImpl(UserRepository userRepository,
                                  TransactionRepository transactionRepository,
                                  TransactionMapper transactionMapper,
                                  CategoryRepository categoryRepository,
                                  IncomeRepository incomeRepository,
                                  AccountRepository accountRepository,
                                  IAccountService accountService,
                                  CreditCardRepository creditCardRepository,
                                  ICreditCardService creditCardService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.categoryRepository = categoryRepository;
        this.incomeRepository = incomeRepository;
        this.accountRepository = accountRepository;
        this.accountService = accountService;
        this.creditCardRepository = creditCardRepository;
        this.creditCardService = creditCardService;
    }

    //Completed abstract methods from GenericServiceImpl
    @Override
    protected String getEntityName() {
        return "Transaction";
    }

    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected TransactionRepository getGenericRepository() {
        return transactionRepository;
    }

    @Override
    protected TransactionMapper getGenericMapper() {
        return transactionMapper;
    }
    

    // Overriding Create and Update methods to add custom logging 

    /**
     * Creates a new Transaction based on the provided DTO and user email.
     * @param entityCreateDTO The DTO containing transaction creation data.
     * @param userEmail The email of the user creating the transaction.
     * @return The created Transaction as a DTO.
     */
    @Override
    public TransactionResponseDTO create(TransactionCreateDTO entityCreateDTO, String userEmail) {
        LOG.infof("Starting creation of %s for user email: %s", getEntityName(), userEmail);

        User user = validateAndGetUser(userEmail);

        Transaction transaction = validateCreateAndReturnEntityByType(entityCreateDTO, user);
       
        transactionRepository.persist(transaction);

        LOG.infof("Successfully created %s with ID: %d for user email: %s", getEntityName(), transaction.id, userEmail);
        return transactionMapper.toDTO(transaction);
    }

    /**
     * Updates a Transaction based on the provided resourceCode, DTO and user email
     * @param resourceCode The resourceCode of the transaction to update.
     * @param entityUpdateDTO The DTO containing transaction update data.
     * @userEmail The email of the user updating the transaction.
     * @return The updated Transaction as a DTO
     */
    @Override
    public TransactionResponseDTO update(String resourceCode, TransactionUpdateDTO entityUpdateDTO, String userEmail) {
        LOG.infof("Starting update of %s with resourceCode %s for user email: %s", entityUpdateDTO, resourceCode, userEmail);

        User user = validateAndGetUser(userEmail);

        Transaction transaction = validateAndGetTransactionEntity(resourceCode, user);

        boolean updated = transactionMapper.updateEntity(entityUpdateDTO, transaction);

        if (entityUpdateDTO.categoryResourceCode() != null) {
            Category category = categoryRepository.findByResourceCodeAndUserId(entityUpdateDTO.categoryResourceCode(), user.id)
                .orElseThrow(() -> {
                    LOG.errorf("Category not found with resource code: %s for user email: %s", entityUpdateDTO.categoryResourceCode(), userEmail);
                    throw new ResourceNotFoundException("Category not found with resource code: " + entityUpdateDTO.categoryResourceCode() + " for user email: " + userEmail);
                });
            
            if (!entityUpdateDTO.categoryResourceCode().equals(category.getResourceCode())) {
                category.setLastUsedDate(transaction.getTransactionDate());
                category.setUsageCount(category.getUsageCount() + 1);
                categoryRepository.persist(category);

                transaction.getCategory().setUsageCount(transaction.getCategory().getUsageCount() - 1);
                categoryRepository.persist(transaction.getCategory());

                transaction.setCategory(category);
                updated = true;
            }
        } else if (entityUpdateDTO.categoryResourceCode() == null && transaction.getCategory() != null) {
            transaction.getCategory().setUsageCount(transaction.getCategory().getUsageCount() - 1);
            categoryRepository.persist(transaction.getCategory());

            transaction.setCategory(null);
            updated = true;
        }

        if (updated) {
            transactionRepository.persist(transaction);
            LOG.infof("%s updated with resource code: %s", getEntityName(), resourceCode);
        } else {
            LOG.infof("No changes detected for %s with resource code: %s", getEntityName(), resourceCode);
        }

        return transactionMapper.toDTO(transaction);
    }

    /**
     * Rolls back a transaction based on the provided resource code and user email.
     * @param resourceCode The resource code of the transaction to be rolled back.
     * @param userEmail The email of the user requesting the rollback.
     */
    @Override
    public void rollback(String resourceCode, String userEmail) {
        LOG.infof("Rolling back transaction with resource code: %s for user email: %s", resourceCode, userEmail);

        User user = validateAndGetUser(userEmail);
        Transaction transaction = validateAndGetTransactionEntity(resourceCode, user);
    
        validateRollbackAndReturnEntityByType(transaction, user);
        
        transactionRepository.delete(transaction);
        LOG.infof("Successfully rolled back transaction with resource code: %s for user email: %s", resourceCode, userEmail);
    }

    /**
     * Filters transactions based on the provided filter DTO and user email.
     * @param filterDTO The DTO containing filter criteria.
     * @param userEmail The email of the user requesting the filtered transactions.
     * @return A pageable DTO containing the filtered transactions.
     */
    @Override
    public APIPageableDTO<TransactionResponseDTO> filterTransactions(TransactionFilterDTO filterDTO, String userEmail) {
        LOG.infof("Filtering transactions for user email: %s with filter: %s", userEmail, filterDTO);

        User user = validateAndGetUser(userEmail);

        List<Transaction> filteredTransactions = transactionRepository.findByFilter(filterDTO, user);
        Integer totalElements = transactionRepository.countByFilter(filterDTO, user);
        
        LOG.infof("Found %d transactions for user email: %s with filter: %s", filteredTransactions.size(), userEmail, filterDTO);

        return APIPageableDTO.generatePageableDTO(
            filterDTO.pageNumber(),
            filterDTO.pageSize(),
            totalElements,
            transactionMapper.toDTOList(filteredTransactions)
        );
    }

    // Auxiliary private methods

    /**
     * Validates the rollback of a transaction based on its type.
     * @param transaction the transaction to be rolled back
     * @param user the user associated with the transaction
     */
    private void validateRollbackAndReturnEntityByType(Transaction transaction, User user) {
        LOG.debugf("Validating rollback for %s ID: %d", getEntityName(), transaction.id);

        LOG.debugf("Processing transaction type: %s", transaction.getTransactionType());
        switch (transaction.getTransactionType()) {
            case EXPENSE -> rollbackExpense(transaction, user);
            case INCOME -> rollbackIncome(transaction, user);
            case TRANSFER -> rollbackTransfer(transaction, user);
            case CREDIT_PAYMENT -> rollbackCreditPayment(transaction, user);
            default -> throw new BadRequestException(getEntityName(), "transactionType", "is invalid");
        };
    }

    /**
     * Validates the TransactionCreateDTO based on the transaction type and returns the corresponding Transaction entity.
     * @param entityCreateDTO The DTO containing transaction creation data.
     * @param user The user associated with the transaction.
     * @return The validated Transaction entity.
     */
    private Transaction validateCreateAndReturnEntityByType(TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Validating creation DTO by type for %s", getEntityName());

        Transaction transaction = transactionMapper.toEntity(entityCreateDTO, user);
        Category category = getCategoryEntity(entityCreateDTO.categoryResourceCode(), user);
        transaction.setCategory(category);

        LOG.debugf("Processing transaction type: %s", entityCreateDTO.transactionType());
        return switch (entityCreateDTO.transactionType()) {
            case EXPENSE -> handleExpense(transaction, entityCreateDTO, user);
            case INCOME -> handleIncome(transaction, entityCreateDTO, user);
            case TRANSFER -> handleTransfer(transaction, entityCreateDTO, user);
            case CREDIT_PAYMENT -> handleCreditPayment(transaction, entityCreateDTO, user);
            default -> throw new BadRequestException(getEntityName(), "transactionType", "is invalid");
                
        };
    }

    /**
     * Gets all INCOME transactions for a given income and user.
     * @param income the income entity
     * @param user the user entity
     * @return a list of transactions associated with the income and user
     */
    private List<Transaction> getAllIncomeTransactionsByIncomeAndUser(Income income, User user) {
        LOG.debugf("Fetching all INCOME transactions for Income ID: %d and User ID: %d", income.id, user.id);
        return transactionRepository.findByIncomeIdAndUserIdAndType(income.id, user.id, TransactionType.INCOME);
    }

    /**
     * Rolls back INCOME transactions by deducting the amount from the associated account.
     * @param transaction The Transaction entity to be rolled back.
     * @param user The user associated with the transaction.
     */
    private void rollbackExpense(Transaction transaction, User user) {
        LOG.debugf("Rolling back EXPENSE transaction ID: %d", transaction.id);
        
        if (transaction.getAccount() != null) {
            accountService.processAddAmount(transaction.getAccount().id, transaction.getAmount());

            transaction.getAccount().setUsageCount(transaction.getAccount().getUsageCount() - 1);
            accountRepository.persist(transaction.getAccount());

            LOG.debugf("Reverted amount to Account ID: %d", transaction.getAccount().id);
        } else if (transaction.getCreditCard() != null) {
            creditCardService.processAddPayment(transaction.getCreditCard().id, transaction.getAmount());

            transaction.getCreditCard().setUsageCount(transaction.getCreditCard().getUsageCount() - 1);
            creditCardRepository.persist(transaction.getCreditCard());

            LOG.debugf("Reverted amount from CreditCard ID: %d", transaction.getCreditCard().id);
        } else {
            LOG.debugf("No associated Account or CreditCard found for EXPENSE transaction ID: %d", transaction.id);
            throw new BadRequestException(getEntityName(), "account/creditCard", "no associated account or credit card found for EXPENSE transaction");
        }
    }

    /**
     * Rolls back INCOME transactions by deducting the amount from the associated account.
     * @param transaction The Transaction entity to be rolled back.
     * @param user The user associated with the transaction.
     */
    private void rollbackIncome(Transaction transaction, User user) {
        LOG.debugf("Rolling back INCOME transaction ID: %d", transaction.id);
        
        accountService.processSubtractAmount(transaction.getAccount().id, transaction.getAmount());
        LOG.debugf("Deducted amount from Account ID: %d", transaction.getAccount().id);
    }

    /**
     * Rolls back TRANSFER transactions by reversing the amount between the source and destination accounts.
     * @param transaction The Transaction entity to be rolled back.
     * @param user The user associated with the transaction.
     */
    private void rollbackTransfer(Transaction transaction, User user) {
        LOG.debugf("Rolling back TRANSFER transaction ID: %d", transaction.id);

        accountService.processSubtractAmount(transaction.getDestinationAccount().id, transaction.getAmount());
        accountService.processAddAmount(transaction.getAccount().id, transaction.getAmount());
        
        LOG.debugf("Reversed transfer between Account ID: %d and Account ID: %d", transaction.getAccount().id, transaction.getDestinationAccount().id);
    }

    /**
     * Rolls back CREDIT_PAYMENT transactions by reversing the payment between the account and credit card.
     * @param transaction The Transaction entity to be rolled back.
     * @param user The user associated with the transaction.
     */
    private void rollbackCreditPayment(Transaction transaction, User user) {
        LOG.debugf("Rolling back CREDIT_PAYMENT transaction ID: %d", transaction.id);

        creditCardService.processAddAmount(transaction.getCreditCard().id, transaction.getAmount());
        accountService.processAddAmount(transaction.getAccount().id, transaction.getAmount());

        Account account = transaction.getAccount();
        account.setUsageCount(account.getUsageCount() - 1);
        accountRepository.persist(account);

        Category category = transaction.getCategory();
        if (category != null) {
            category.setUsageCount(category.getUsageCount() - 1);
            categoryRepository.persist(category);
        }
        
        LOG.debugf("Reversed credit payment between Account ID: %d and CreditCard ID: %d", transaction.getAccount().id, transaction.getCreditCard().id);
    }

    /**
     * Handles EXPENSE transactions. Options include deducting from an account or adding to a credit card.
     * @param transaction The Transaction entity to be processed.
     * @param entityCreateDTO The DTO containing transaction creation data.
     * @param user The user associated with the transaction.
     * @return The processed Transaction entity.
     */
    private Transaction handleExpense(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling EXPENSE transaction");
        
        if (!isValid(entityCreateDTO.accountResourceCode()) && !isValid(entityCreateDTO.creditCardResourceCode())) {
            LOG.debugf("Either accountResourceCode or creditCardResourceCode must be provided for EXPENSE transactions");
            throw new BadRequestException(getEntityName(), "accountResourceCode/creditCardResourceCode", "one of them must be provided for EXPENSE transactions");
        }

        if (isValid(entityCreateDTO.accountResourceCode())) {
            Account account = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
            accountService.processSubtractAmount(account.id, entityCreateDTO.amount());

            account.setLastUsedDate(transaction.getTransactionDate());
            account.setUsageCount(account.getUsageCount() + 1);
            accountRepository.persist(account);

            transaction.setAccount(account);
            LOG.debugf("Expense processed from Account ID: %d", account.id);
        } else if (isValid(entityCreateDTO.creditCardResourceCode())) {
            CreditCard creditCard = validateAndGetCreditCardEntity(entityCreateDTO.creditCardResourceCode(), user);
            creditCardService.processAddAmount(creditCard.id, entityCreateDTO.amount());

            creditCard.setLastUsedDate(transaction.getTransactionDate());
            creditCard.setUsageCount(creditCard.getUsageCount() + 1);
            creditCardRepository.persist(creditCard);

            transaction.setCreditCard(creditCard);
            LOG.debugf("Expense processed to CreditCard ID: %d", creditCard.id);
        }

        if (transaction.getCategory() != null) {
            transaction.getCategory().setLastUsedDate(transaction.getTransactionDate());
            transaction.getCategory().setUsageCount(transaction.getCategory().getUsageCount() + 1);
            categoryRepository.persist(transaction.getCategory());
        }

        return transaction;
    }

    /**
     * Handles INCOME transactions. Involves validating income and account entities and ensuring total income limits.
     * @param transaction The Transaction entity to be processed.
     * @param entityCreateDTO The DTO containing transaction creation data.
     * @param user The user associated with the transaction.
     * @return The processed Transaction entity.
     */
    private Transaction handleIncome(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling INCOME transaction");

        if (!isValid(entityCreateDTO.incomeResourceCode()) || !isValid(entityCreateDTO.accountResourceCode())) {
            LOG.debugf("Both incomeResourceCode and accountResourceCode must be provided for INCOME transactions");
            throw new BadRequestException(getEntityName(), "incomeResourceCode/accountResourceCode", "both must be provided for INCOME transactions");
        }

        Income income = validateAndGetIncomeEntity(entityCreateDTO.incomeResourceCode(), user);
        Account account = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
        
        validateTotalIncomeDoesNotExceedTransactions(income, user, transaction.getAmount());
        accountService.processAddAmount(account.id, entityCreateDTO.amount());

        transaction.setIncome(income);
        transaction.setAccount(account);

        LOG.debugf("Income processed to Account ID: %d from Income ID: %d", account.id, income.id);

        return transaction;
    }

    /**
     * Handles TRANSFER transactions between two accounts.
     * @param transaction The Transaction entity to be processed.
     * @param entityCreateDTO The DTO containing transaction creation data.
     * @param user The user associated with the transaction.
     * @return The processed Transaction entity.
     */
    private Transaction handleTransfer(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling TRANSFER transaction");

        if (!isValid(entityCreateDTO.accountResourceCode()) || !isValid(entityCreateDTO.destinationAccountResourceCode())) {
            LOG.debugf("Both accountResourceCode and destinationAccountResourceCode must be provided for TRANSFER transactions");
            throw new BadRequestException(getEntityName(), "accountResourceCode/destinationAccountResourceCode", "both must be provided for TRANSFER transactions");
        }

        Account sourceAccount = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
        Account destinationAccount = validateAndGetAccountEntity(entityCreateDTO.destinationAccountResourceCode(), user);
        
        accountService.processSubtractAmount(sourceAccount.id, entityCreateDTO.amount());
        accountService.processAddAmount(destinationAccount.id, entityCreateDTO.amount());

        transaction.setAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);

        LOG.debugf("Transfer processed from Account ID: %d to Account ID: %d", sourceAccount.id, destinationAccount.id);

        return transaction;
    }

    /**
     * Handles CREDIT_PAYMENT transactions between an account and a credit card.
     * @param transaction The Transaction entity to be processed.
     * @param entityCreateDTO The DTO containing transaction creation data.
     * @param user The user associated with the transaction.
     * @return The processed Transaction entity.
     */
    private Transaction handleCreditPayment(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling CREDIT_PAYMENT transaction");

        if (!isValid(entityCreateDTO.accountResourceCode()) || !isValid(entityCreateDTO.creditCardResourceCode())) {
            LOG.debugf("Both accountResourceCode and creditCardResourceCode must be provided for CREDIT_PAYMENT transactions");
            throw new BadRequestException(getEntityName(), "accountResourceCode/creditCardResourceCode", "both must be provided for CREDIT_PAYMENT transactions");
        }

        Account account = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
        CreditCard creditCard = validateAndGetCreditCardEntity(entityCreateDTO.creditCardResourceCode(), user);
        
        accountService.processSubtractAmount(account.id, entityCreateDTO.amount());
        creditCardService.processAddPayment(creditCard.id, entityCreateDTO.amount());

        account.setUsageCount(account.getUsageCount() + 1);
        account.setLastUsedDate(transaction.getTransactionDate());

        accountRepository.persist(account);

        Category category = transaction.getCategory();
        if (category != null) {
            category.setLastUsedDate(transaction.getTransactionDate());
            category.setUsageCount(category.getUsageCount() + 1);
            categoryRepository.persist(category);
        }
        
        transaction.setAccount(account);
        transaction.setCreditCard(creditCard);

        LOG.debugf("Credit payment processed from Account ID: %d to CreditCard ID: %d", account.id, creditCard.id);

        return transaction;
    }

    private Category getCategoryEntity(String categoryResourceCode, User user) {
        return categoryRepository.findByResourceCodeAndUserId(categoryResourceCode, user.id)
                .orElse(null);
    }

    private Transaction validateAndGetTransactionEntity(String transactionResourceCode, User user) {
        return validateAndGetEntityByResourceCode(transactionResourceCode, user, transactionRepository::findByResourceCodeAndUserId, "Transaction");
    }

    private Income validateAndGetIncomeEntity(String incomeResourceCode, User user) {
        return validateAndGetEntityByResourceCode(incomeResourceCode, user, incomeRepository::findByResourceCodeAndUserId, "Income");
    }

    private Account validateAndGetAccountEntity(String accountResourceCode, User user) {
        return validateAndGetEntityByResourceCode(accountResourceCode, user, accountRepository::findByResourceCodeAndUserId, "Account");
    }

    private CreditCard validateAndGetCreditCardEntity(String creditCardResourceCode, User user) {
        return validateAndGetEntityByResourceCode(creditCardResourceCode, user, creditCardRepository::findByResourceCodeAndUserId, "Credit Card");
    }

    private <T> T validateAndGetEntityByResourceCode(String resourceCode, User user, 
            BiFunction<String, Long, Optional<T>> finder, String entityName) {
        return finder.apply(resourceCode, user.id)
                .orElseThrow(() -> {
                    LOG.debugf("%s with resource code %s not found for user ID %d", entityName, resourceCode, user.id);
                    throw new BadRequestException(getEntityName(), entityName + " resource code", "is invalid");
                });
    }

    private void validateTotalIncomeDoesNotExceedTransactions(Income income, User user, BigDecimal newAmount) {
        List<Transaction> incomeTransactions = getAllIncomeTransactionsByIncomeAndUser(income, user);
        BigDecimal totalIncomeFromTransactions = incomeTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalIncomeFromTransactions.add(newAmount).compareTo(income.getTotalAmount()) > 0) {
            LOG.debugf("Total income from transactions exceeds Income ID: %d total amount", income.id);
            throw new BadRequestException(getEntityName(), "amount", "exceeds the total income limit");
        }
    }

    private boolean isValid(Object value) {
        return value != null;
    }
}