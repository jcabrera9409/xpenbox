import { Injectable } from '@angular/core';
import { TransactionResponseDTO } from '../model/transaction.response.dto';
import { TransactionRequestDTO } from '../model/transaction.request.dto';
import { GenericService } from '../../common/service/generic.service';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { transactionState } from './transaction.state';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { TransactionFilterRequestDTO } from '../model/transaction-filter.request.dto';
import { PageableResponseDTO } from '../../common/model/pageable.response.dto';
import { Observable } from 'rxjs';
import { upgradeProModalState } from '../../../modal/subscription/state/upgrade-pro.modal.state';

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
   * Filters transactions based on the provided filter criteria.
   * @param filter The filter criteria encapsulated in a TransactionFilterRequestDTO object.
   * @returns An Observable emitting an ApiResponseDTO containing a pageable list of filtered TransactionResponseDTOs.
   */
  filterTransactions(filter: TransactionFilterRequestDTO): Observable<ApiResponseDTO<PageableResponseDTO<TransactionResponseDTO, TransactionFilterRequestDTO>>> {
    return this.http.post<ApiResponseDTO<PageableResponseDTO<TransactionResponseDTO, TransactionFilterRequestDTO>>>(
      `${this.apiUrl}/filter`, filter, { withCredentials: true }
    );
  }

  /**
   * Deletes a transaction by its resource code.
   * @param resourceCode The unique identifier of the transaction to be deleted.
   * @returns An Observable emitting an ApiResponseDTO indicating the result of the delete operation.
   */
  delete(resourceCode: string): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${resourceCode}`, { withCredentials: true }
    );
  }

  submitTransaction(transactionRequest:TransactionRequestDTO, success:() => void): void {
    transactionState.isLoadingSendingTransaction.set(true);
    transactionState.errorSendingTransaction.set(null);

    this.create(transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        transactionState.isLoadingSendingTransaction.set(false);
        transactionState.successSendingTransaction.set(true);
        transactionState.transactionCreatedResourceCode.set(response.data.resourceCode);
        success();
      }, error: (error) => {
        transactionState.isLoadingSendingTransaction.set(false);
        if (error.status === 500 || error.status === 0) {
          transactionState.errorSendingTransaction.set('Error guardando la transacción. Por favor, inténtalo de nuevo.');
        } else if (error.status === 403) {
          if (error.error && error.error.featureCode) {
            this.showUpgradeProModal();
          } else {
            transactionState.errorSendingTransaction.set('No tienes permiso para realizar esta acción. Por favor, contacta con soporte.');
          } 
        } else {
          transactionState.errorSendingTransaction.set(error.error.message || 'Error guardando la transacción.');
        }
      }
    });
  }

  private showUpgradeProModal(): void {
    upgradeProModalState.title.set('¡Estás usando Xpenbox a todo ritmo!');
    upgradeProModalState.htmlMessage.set('Ya registraste 50 transacciones en tu plan Free.' +
              ' Actualiza a <strong>Pro</strong> para seguir registrando todos tus gastos, ingresos y movimientos sin límites.');
    upgradeProModalState.open.set(true);
  }

  /**
   * Loads transactions and updates the transaction state with the fetched data.
   * Handles loading and error states.
   */
  override load(): void {
      transactionState.isLoadingGetList.set(true);
      transactionState.errorGetList.set(null);
  
      this.getAll().subscribe({
        next: (response: ApiResponseDTO<TransactionResponseDTO[]>) => {
          transactionState.transactions.set(response.data || []);
          transactionState.isLoadingGetList.set(false);
        },
        error: (error) => {
          console.error('Error fetching transactions:', error);
          transactionState.errorGetList.set(error.message || 'Error fetching transactions');
          transactionState.isLoadingGetList.set(false);
        }
      })
    }
}
