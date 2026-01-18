package org.xpenbox.transaction.service.impl;

import java.util.Optional;
import java.util.function.BiFunction;

import org.jboss.logging.Logger;
import org.xpenbox.account.entity.Account;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.category.entity.Category;
import org.xpenbox.category.repository.CategoryRepository;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.creditcard.entity.CreditCard;
import org.xpenbox.creditcard.repository.CreditCardRepository;
import org.xpenbox.creditcard.service.ICreditCardService;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.income.entity.Income;
import org.xpenbox.income.repository.IncomeRepository;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;
import org.xpenbox.transaction.dto.TransactionUpdateDTO;
import org.xpenbox.transaction.entity.Transaction;
import org.xpenbox.transaction.mapper.TransactionMapper;
import org.xpenbox.transaction.repository.TransactionRepository;
import org.xpenbox.transaction.service.ITransactionService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

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

        User user = super.validateAndGetUser(userEmail);

        Transaction transaction = validateCreateAndReturnEntityByType(entityCreateDTO, user);
       
        transactionRepository.persist(transaction);

        LOG.infof("Successfully created %s with ID: %d for user email: %s", getEntityName(), transaction.id, userEmail);
        return transactionMapper.toDTO(transaction);
    }


    // Auxiliary private methods

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
            transaction.setAccount(account);
            LOG.debugf("Expense processed from Account ID: %d", account.id);
        } else if (isValid(entityCreateDTO.creditCardResourceCode())) {
            CreditCard creditCard = validateAndGetCreditCardEntity(entityCreateDTO.creditCardResourceCode(), user);
            creditCardService.processAddAmount(creditCard.id, entityCreateDTO.amount());
            transaction.setCreditCard(creditCard);
            LOG.debugf("Expense processed to CreditCard ID: %d", creditCard.id);
        }

        return transaction;
    }

    private Transaction handleIncome(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling INCOME transaction");

        if (!isValid(entityCreateDTO.incomeResourceCode()) || !isValid(entityCreateDTO.accountResourceCode())) {
            LOG.debugf("Both incomeResourceCode and accountResourceCode must be provided for INCOME transactions");
            throw new BadRequestException(getEntityName(), "incomeResourceCode/accountResourceCode", "both must be provided for INCOME transactions");
        }

        Income income = validateAndGetIncomeEntity(entityCreateDTO.incomeResourceCode(), user);
        Account account = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
        transaction.setIncome(income);
        transaction.setAccount(account);

        return transaction;
    }

    private Transaction handleTransfer(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling TRANSFER transaction");

        if (!isValid(entityCreateDTO.accountResourceCode()) || !isValid(entityCreateDTO.destinationAccountResourceCode())) {
            LOG.debugf("Both accountResourceCode and destinationAccountResourceCode must be provided for TRANSFER transactions");
            throw new BadRequestException(getEntityName(), "accountResourceCode/destinationAccountResourceCode", "both must be provided for TRANSFER transactions");
        }

        Account sourceAccount = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
        Account destinationAccount = validateAndGetAccountEntity(entityCreateDTO.destinationAccountResourceCode(), user);
        transaction.setAccount(sourceAccount);
        transaction.setDestinationAccount(destinationAccount);

        return transaction;
    }

    private Transaction handleCreditPayment(Transaction transaction, TransactionCreateDTO entityCreateDTO, User user) {
        LOG.debugf("Handling CREDIT_PAYMENT transaction");

        if (!isValid(entityCreateDTO.accountResourceCode()) || !isValid(entityCreateDTO.creditCardResourceCode())) {
            LOG.debugf("Both accountResourceCode and creditCardResourceCode must be provided for CREDIT_PAYMENT transactions");
            throw new BadRequestException(getEntityName(), "accountResourceCode/creditCardResourceCode", "both must be provided for CREDIT_PAYMENT transactions");
        }

        Account account = validateAndGetAccountEntity(entityCreateDTO.accountResourceCode(), user);
        CreditCard creditCard = validateAndGetCreditCardEntity(entityCreateDTO.creditCardResourceCode(), user);
        transaction.setAccount(account);
        transaction.setCreditCard(creditCard);

        return transaction;
    }

    private Category getCategoryEntity(String categoryResourceCode, User user) {
        return categoryRepository.findByResourceCodeAndUserId(categoryResourceCode, user.id)
                .orElse(null);
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

    private boolean isValid(Object value) {
        return value != null;
    }
}