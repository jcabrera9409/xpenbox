package org.xpenbox.account.service;

import java.util.List;

import org.xpenbox.account.dto.AccountCreateDTO;
import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.account.dto.AccountUpdateDTO;

/**
 * Account Service Interface
 */
public interface IAccountService {
    
    /**
     * Create a new account 
     * @param accountCreateDTO the account creation data transfer object
     * @param userEmail the email of the user creating the account
     * @return the created account response data transfer object
     */
    AccountResponseDTO create(AccountCreateDTO accountCreateDTO, String userEmail);

    /**
     * Update an existing account
     * @param resourceCode the resource code of the account to be updated
     * @param accountUpdateDTO the account update data transfer object
     * @param userEmail the email of the user updating the account
     * @return the updated account response data transfer object
     */
    AccountResponseDTO update(String resourceCode, AccountUpdateDTO accountUpdateDTO, String userEmail);

    /**
     * Get an account by its resource code 
     * @param resourceCode the resource code of the account 
     * @param userEmail the email of the user requesting the account
     * @return the account response data transfer object
     */
    AccountResponseDTO getByResourceCode(String resourceCode, String userEmail);

    /**
     * Get all accounts for a user
     * @param userEmail the email of the user
     * @return a list of account response data transfer objects
     */
    List<AccountResponseDTO> getAll(String userEmail);
}
