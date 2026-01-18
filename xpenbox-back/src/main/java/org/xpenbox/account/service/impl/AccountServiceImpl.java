package org.xpenbox.account.service.impl;

import java.math.BigDecimal;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.account.mapper.AccountMapper;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.exception.InsufficientFoundsException;
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

    public AccountServiceImpl(
        UserRepository userRepository,
        AccountRepository accountRepository,
        AccountMapper accountMapper
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
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
    public void processSubtractAmount(Long id, BigDecimal amount) {
        LOG.infof("Processing subtract amount for Account ID: %d with amount: %s", id, amount);

        Account account = accountRepository.findByIdOptional(id)
            .orElseThrow(() -> {
                LOG.debugf("Account with ID %d not found", id);
                return new BadRequestException("Account not found");
            });

        if (account.getBalance().compareTo(amount) < 0) {
            LOG.debugf("Insufficient funds for Account ID %d: Current balance %s, Requested amount %s", id, account.getBalance(), amount);
            throw new InsufficientFoundsException("Insufficient funds for the transaction");
        }
        account.setBalance(account.getBalance().subtract(amount));

        accountRepository.persist(account);
        LOG.infof("Transaction processed successfully for Account ID: %d. New balance: %s", id, account.getBalance());
    }

    @Override
    public void processAddAmount(Long id, BigDecimal amount) {
        LOG.infof("Processing add amount for Account ID: %d with amount: %s", id, amount);
        Account account = accountRepository.findByIdOptional(id)
            .orElseThrow(() -> {
                LOG.debugf("Account with ID %d not found", id);
                return new BadRequestException("Account not found");
            });
        account.setBalance(account.getBalance().add(amount));
        accountRepository.persist(account);
        LOG.infof("Amount added successfully for Account ID: %d. New balance: %s", id, account.getBalance());
    }

}
