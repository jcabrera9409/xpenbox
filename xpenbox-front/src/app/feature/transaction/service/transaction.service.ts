import { Injectable } from '@angular/core';
import { TransactionResponseDTO } from '../model/transaction.response.dto';
import { TransactionRequestDTO } from '../model/transaction.request.dto';
import { GenericService } from '../../common/service/generic.service';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { transactionState } from './transaction.state';
import { ApiResponseDTO } from '../../common/model/api.response.dto';

/**
 * Service for managing transactions, including creating, updating, and fetching transaction data.
 * This service interacts with the backend API to perform CRUD operations on transactions.
 * It also updates the transaction state to reflect the current list of transactions.
 */
@Injectable({
  providedIn: 'root',
})
export class TransactionService extends GenericService<TransactionRequestDTO, TransactionResponseDTO> {

  constructor(
    protected override http: HttpClient,
    protected envService: EnvService
  ) {
    super ( 
      http,
      `${envService.getApiUrl()}/transaction`
    )
  }

  /**
   * Loads transactions and updates the transaction state with the fetched data.
   * Handles loading and error states.
   */
  override load(): void {
      transactionState.isLoading.set(true);
      transactionState.error.set(null);
  
      this.getAll().subscribe({
        next: (response: ApiResponseDTO<TransactionResponseDTO[]>) => {
          transactionState.transactions.set(response.data || []);
          transactionState.isLoading.set(false);
        },
        error: (error) => {
          console.error('Error fetching transactions:', error);
          transactionState.error.set(error.message || 'Error fetching transactions');
          transactionState.isLoading.set(false);
        }
      })
    }
}
