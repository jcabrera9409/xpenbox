package org.xpenbox.account.service;

import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.common.service.IGenericService;

/**
 * Account Service Interface
 */
public interface IAccountService extends IGenericService<Account, AccountCreateDTO, AccountUpdateDTO, AccountResponseDTO> {
    
}
