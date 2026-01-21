import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountResponseDTO } from '../model/accountResponseDTO';
import { accountState } from './account.state';
import { ApiResponseDTO } from '../../common/model/apiResponseDTO';

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

  getAccounts(): Observable<ApiResponseDTO<AccountResponseDTO[]>> {
    return this.http.get<ApiResponseDTO<AccountResponseDTO[]>>(this.apiUrl, { withCredentials: true });
  }

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

  refreshAccounts(): void {
    this.loadAccounts();
  }
}
