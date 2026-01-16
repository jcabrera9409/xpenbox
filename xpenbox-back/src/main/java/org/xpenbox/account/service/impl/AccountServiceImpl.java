package org.xpenbox.account.service.impl;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.account.mapper.AccountMapper;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Account Service Implementation
 */
@ApplicationScoped
public class AccountServiceImpl implements IAccountService {
    private static final Logger LOG = Logger.getLogger(AccountServiceImpl.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountServiceImpl(
        UserRepository userRepository,
        AccountRepository accountRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountResponseDTO create(AccountCreateDTO accountCreateDTO, String userEmail) {
        LOG.infof("Creating account for user email: %s", userEmail);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                throw new UnauthorizedException("User not found with email: " + userEmail); 
            });

        Account newAccount = AccountMapper.toEntity(accountCreateDTO);
        newAccount.setResourceCode(ResourceCode.generateAccountResourceCode());
        newAccount.setUser(user);

        accountRepository.persist(newAccount);
        LOG.infof("Account created with resource code: %s", newAccount.getResourceCode());

        return AccountMapper.toDTO(newAccount);
    }

    @Override
    public AccountResponseDTO update(String resourceCode, AccountUpdateDTO accountUpdateDTO, String userEmail) {
        LOG.infof("Updating account with resource code: %s for user email: %s", resourceCode, userEmail);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                throw new UnauthorizedException("User not found with email: " + userEmail); 
            });
        
        Account existingAccount = accountRepository.findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("Account not found with resource code: %s", resourceCode);
                throw new ResourceNotFoundException("Account not found with resource code: " + resourceCode);
            });

        boolean isUpdated = AccountMapper.toUpdateEntity(accountUpdateDTO, existingAccount);
        if (isUpdated) {
            accountRepository.persist(existingAccount);
            LOG.infof("Account with resource code: %s updated successfully", resourceCode);
        } else {
            LOG.infof("No changes detected for account with resource code: %s", resourceCode);
        }

        return AccountMapper.toDTO(existingAccount);
    }

    @Override
    public AccountResponseDTO getByResourceCode(String resourceCode, String userEmail) {
        LOG.infof("Retrieving account with resource code: %s for user email: %s", resourceCode, userEmail);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                throw new UnauthorizedException("User not found with email: " + userEmail); 
            });
        
        Account existingAccount = accountRepository.findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("Account not found with resource code: %s", resourceCode);
                throw new ResourceNotFoundException("Account not found with resource code: " + resourceCode);
            });

        return AccountMapper.toDTO(existingAccount);
    }

    @Override
    public List<AccountResponseDTO> getAll(String userEmail) {
        LOG.infof("Retrieving all accounts for user email: %s", userEmail);
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                throw new UnauthorizedException("User not found with email: " + userEmail); 
            });

        List<Account> accounts = accountRepository.findAllByUserId(user.id);
        LOG.infof("Found %d accounts for user email: %s", accounts.size(), userEmail);

        return AccountMapper.toDTOList(accounts);
    }
    
}
