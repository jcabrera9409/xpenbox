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
import jakarta.inject.Inject;

/**
 * Account Service Implementation
 */
@ApplicationScoped
public class AccountServiceImpl extends GenericServiceImpl<Account, AccountCreateDTO, AccountUpdateDTO, AccountResponseDTO> implements IAccountService {
    
    public AccountServiceImpl() {
        super();
    }
    
    @Inject
    public AccountServiceImpl(
        UserRepository userRepository,
        AccountRepository accountRepository,
        AccountMapper accountMapper
    ) {
        super("Account", userRepository, accountRepository, accountMapper);
    }
}
