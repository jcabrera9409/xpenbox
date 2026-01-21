import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountResponseDTO } from '../model/accountResponseDTO';
import { accountState } from './account.state';
import { ApiResponseDTO } from '../../common/model/apiResponseDTO';
import { AccountRequestDTO } from '../model/accountRequestDTO';

/**
 * Service for managing accounts, including creating, updating, and fetching account data.
 * This service interacts with the backend API to perform CRUD operations on accounts.
 * It also updates the account state to reflect the current list of accounts and their total liquid balance.
 */
@Injectable({
  providedIn: 'root',
})
export class AccountService {

  private apiUrl: string;
  
  constructor(
    private http: HttpClient,
    private envService: EnvService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/account`;
  }

  /**
   * Creates a new account.
   * @param accountData The data for the account to be created.
   * @returns An observable of the API response containing the created account.
   */
  createAccount(accountData: AccountRequestDTO): Observable<ApiResponseDTO<AccountResponseDTO>> {
    return this.http.post<ApiResponseDTO<AccountResponseDTO>>(this.apiUrl, accountData, { withCredentials: true });
  }

  /**
   * Updates an existing account.
   * @param resourceCode The resource code of the account to be updated.
   * @param accountData The updated data for the account.
   * @returns An observable of the API response containing the updated account.
   */
  updateAccount(resourceCode: string, accountData: AccountRequestDTO): Observable<ApiResponseDTO<AccountResponseDTO>> {
    return this.http.put<ApiResponseDTO<AccountResponseDTO>>(`${this.apiUrl}/${resourceCode}`, accountData, { withCredentials: true });
  }

  /**
   * Fetches an account by its resource code.
   * @param resourceCode The resource code of the account to be fetched.
   * @returns An observable of the API response containing the requested account.
   */
  getAccountByResourceCode(resourceCode: string): Observable<ApiResponseDTO<AccountResponseDTO>> {
    return this.http.get<ApiResponseDTO<AccountResponseDTO>>(`${this.apiUrl}/${resourceCode}`, { withCredentials: true });
  }

  /**
   * Fetches all accounts.
   * @returns An observable of the API response containing the list of accounts.
   */
  getAccounts(): Observable<ApiResponseDTO<AccountResponseDTO[]>> {
    return this.http.get<ApiResponseDTO<AccountResponseDTO[]>>(this.apiUrl, { withCredentials: true });
  }

  /**
   * Loads accounts and updates the account state with the fetched data.
   * Calculates the total liquid balance of all accounts and updates the state accordingly.
   * Handles loading and error states.
   */
  loadAccounts(): void {
    accountState.isLoading.set(true);
    accountState.error.set(null);

    this.getAccounts().subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO[]>) => {
        const totalBalance = response.data?.reduce((sum, account) => sum + (account.balance || 0), 0) || 0;
        accountState.accounts.set(response.data || []);
        accountState.totalLiquidBalance.set(totalBalance);
        accountState.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching accounts:', error);
        accountState.error.set(error.message || 'Error fetching accounts');
        accountState.isLoading.set(false);
      }
    })
  }

  /**
   * Refreshes the list of accounts by reloading them from the backend.
   * This method is typically called after creating or updating an account to ensure
   * the account state reflects the latest data.
   */
  refreshAccounts(): void {
    this.loadAccounts();
  }
}
