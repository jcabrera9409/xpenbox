package org.xpenbox.account.service;

import java.math.BigDecimal;

import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;
import org.xpenbox.account.entity.Account;
import org.xpenbox.common.service.IGenericService;

/**
 * Account Service Interface
 */
public interface IAccountService extends IGenericService<Account, AccountCreateDTO, AccountUpdateDTO, AccountResponseDTO> {
 
    /**
     * Process to subtract amount from account balance
     * @param id the account id
     * @param amount the amount to subtract
     */
    public void processSubtractAmount(Long id, BigDecimal amount);

    /**
     * Process to add amount to account balance
     * @param id the account id
     * @param amount the amount to add
     */
    public void processAddAmount(Long id, BigDecimal amount);
}
