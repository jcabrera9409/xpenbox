package org.xpenbox.account.service.impl;

import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.account.mapper.AccountMapper;
import org.xpenbox.account.repository.AccountRepository;
import org.xpenbox.account.service.IAccountService;
import org.xpenbox.common.service.impl.GenericServiceImpl;

import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Account Service Implementation
 */
@ApplicationScoped
public class AccountServiceImpl extends GenericServiceImpl<Account, AccountCreateDTO, AccountUpdateDTO, AccountResponseDTO> implements IAccountService {
    
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

}
