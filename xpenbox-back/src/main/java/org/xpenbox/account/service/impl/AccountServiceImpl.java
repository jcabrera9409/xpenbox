package org.xpenbox.account.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountDeactivateRequestDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.account.mapper.AccountMapper;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.exception.InsufficientFoundsException;
import org.xpenbox.transaction.dto.TransactionCreateDTO;
import org.xpenbox.transaction.entity.Transaction.TransactionType;
import org.xpenbox.transaction.service.ITransactionService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

/**
 * Account Service Implementation
 */
@ApplicationScoped
public class AccountServiceImpl extends GenericServiceImpl<Account, AccountCreateDTO, AccountUpdateDTO, AccountResponseDTO> implements IAccountService {
    private static final Logger LOG = Logger.getLogger(AccountServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final ITransactionService transactionService;

    public AccountServiceImpl(
        UserRepository userRepository,
        AccountRepository accountRepository,
        AccountMapper accountMapper,
        ITransactionService transactionService
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.transactionService = transactionService;
    }

    @Override
    protected String getEntityName() {
        return "Account";
    }

    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected AccountRepository getGenericRepository() {
        return accountRepository;
    }

    @Override
    protected AccountMapper getGenericMapper() {
        return accountMapper;
    }

    @Override 
    public List<AccountResponseDTO> getAll(String userEmail) {
        return super.getAll(userEmail).stream()
            .filter(AccountResponseDTO::state)
            .toList();
    }

    @Override
    public void processSubtractAmount(String resourceCode, Long userId, BigDecimal amount) {
        LOG.infof("Processing subtract amount for Account with resource code: %s with amount: %s", resourceCode, amount);

        Account account = validateAndGetAccount(resourceCode, userId);

        if (account.getBalance().compareTo(amount) < 0) {
            LOG.debugf("Insufficient funds for Account with resource code: %s. Current balance: %s, Requested amount: %s", resourceCode, account.getBalance(), amount);
            throw new InsufficientFoundsException("Insufficient funds for the transaction");
        }
        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.persist(account);
        LOG.infof("Transaction processed successfully for Account with resource code: %s. New balance: %s", resourceCode, account.getBalance());
    }

    @Override
    public void processAddAmount(String resourceCode, Long userId, BigDecimal amount) {
        LOG.infof("Processing add amount for Account with resource code: %s with amount: %s", resourceCode, amount);
        Account account = validateAndGetAccount(resourceCode, userId);  

        account.setBalance(account.getBalance().add(amount));
        accountRepository.persist(account);
        LOG.infof("Amount added successfully for Account with resource code: %s. New balance: %s", resourceCode, account.getBalance());
    }

    @Override
    public void deactivateAccount(String resourceCode, AccountDeactivateRequestDTO accountDeactivateRequestDTO, String userEmail) {
        LOG.infof("Deactivating account with resource code: %s", resourceCode);

        User user = super.validateAndGetUser(userEmail);

        Account accountToDeactivate = validateAndGetAccount(resourceCode, user.id);

        if (accountToDeactivate.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            LOG.infof("Account with resource code: %s has remaining balance of %s. Transferring to target account before deactivation.", resourceCode, accountToDeactivate.getBalance());
            
            Account targetAccount = validateAndGetAccount(accountDeactivateRequestDTO.accountResourceCodeToTransfer(), user.id);

            TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO(
                TransactionType.TRANSFER, 
                "Auto-generated transfer on account deactivation: " + accountToDeactivate.getName(),
                accountToDeactivate.getBalance(), 
                null, 
                null, 
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), 
                null, 
                null, 
                accountToDeactivate.getResourceCode(), 
                null, 
                targetAccount.getResourceCode()
            );

            transactionService.create(transactionCreateDTO, userEmail);
        }

        accountToDeactivate.setClosingDate(LocalDateTime.now());
        accountToDeactivate.setState(false);

        accountRepository.persist(accountToDeactivate);
        LOG.infof("Account with resource code: %s deactivated successfully", resourceCode);
    }

    private Account validateAndGetAccount(String resourceCode, Long userId) {
        if (resourceCode == null || resourceCode.isEmpty()) {
            LOG.debug("Resource code is null");
            throw new BadRequestException("Account resource code must be provided");
        }
        Account account = accountRepository.findByResourceCodeAndUserId(resourceCode, userId)
            .orElseThrow(() -> {
                LOG.debugf("Account with resource code %s not found for user ID %d", resourceCode, userId);
                return new BadRequestException("Target account for balance transfer not found");
            });

        if (!account.getState()) {
            LOG.debugf("Target account with resource code %s is deactivated", resourceCode);
            throw new BadRequestException("Target account for balance transfer is deactivated");
        }

        return account;
    }

}
