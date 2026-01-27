import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { AccountResponseDTO } from '../model/account.response.dto';
import { accountState } from './account.state';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { AccountRequestDTO } from '../model/account.request.dto';
import { GenericService } from '../../common/service/generic.service';

/**
 * Service for managing accounts, including creating, updating, and fetching account data.
 * This service interacts with the backend API to perform CRUD operations on accounts.
 * It also updates the account state to reflect the current list of accounts and their total liquid balance.
 */
@Injectable({
  providedIn: 'root',
})
export class AccountService extends GenericService<AccountRequestDTO, AccountResponseDTO> {
  
  constructor(
    protected override http: HttpClient,
    protected envService: EnvService
  ) {
    super ( 
      http,
      `${envService.getApiUrl()}/account`
    )
  }

  /**
   * Loads accounts and updates the account state with the fetched data.
   * Calculates the total liquid balance of all accounts and updates the state accordingly.
   * Handles loading and error states.
   */
  override load(): void {
    accountState.isLoadingGetList.set(true);
    accountState.errorGetList.set(null);

    this.getAll().subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO[]>) => {
        const totalBalance = response.data?.reduce((sum, account) => sum + (account.balance || 0), 0) || 0;
        accountState.accounts.set(response.data || []);
        accountState.totalLiquidBalance.set(totalBalance);
        accountState.isLoadingGetList.set(false);
      },
      error: (error) => {
        console.error('Error fetching accounts:', error);
        accountState.errorGetList.set(error.message || 'Error fetching accounts');
        accountState.isLoadingGetList.set(false);
      }
    })
  }
}
