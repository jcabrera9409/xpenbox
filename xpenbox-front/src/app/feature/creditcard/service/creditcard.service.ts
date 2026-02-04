import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { CreditCardResponseDTO } from '../model/creditcard.response.dto';
import { creditCardState } from './creditcard.state';
import { CreditCardRequestDTO } from '../model/creditcard.request.dto';
import { GenericService } from '../../common/service/generic.service';
import { Observable } from 'rxjs/internal/Observable';
import { CreditCardDeactivateRequestDTO } from '../model/creditcard.deactivate.request.dto';

/**
 * Service for managing credit cards, including creating, updating, and fetching credit card data.
 * This service interacts with the backend API to perform CRUD operations on credit cards.
 * It also updates the credit card state to reflect the current list of credit cards and their total balance.
 */
@Injectable({
  providedIn: 'root',
})
export class CreditCardService extends GenericService<CreditCardRequestDTO, CreditCardResponseDTO> {

  constructor(
    protected override http: HttpClient,
    protected envService: EnvService
  ) {
    super ( 
      http,
      `${envService.getApiUrl()}/creditcard`
    )
  }

  /**
   * Deactivate a credit card and pay off remaining balance from another account
   * @param resourceCode The resource code of the credit card to deactivate
   * @param creditCardDeactivateRequestDTO Data transfer object containing the target account resource code for balance transfer
   * @returns An observable of the API response indicating the result of the deactivation operation
   */
  deactivateCreditCard(resourceCode: string, creditCardDeactivateRequestDTO: CreditCardDeactivateRequestDTO): Observable<ApiResponseDTO<void>> {
    return this.http.patch<ApiResponseDTO<void>>(
      `${this.apiUrl}/${resourceCode}/deactivate`,
      creditCardDeactivateRequestDTO,
      { withCredentials: true }
    );
  }

  /**
   * Loads credit cards and updates the credit card state with the fetched data.
   * Calculates the total balance of all credit cards and updates the state accordingly.
   * Handles loading and error states.
   */
  override load(): void {
    creditCardState.isLoadingGetList.set(true);
    creditCardState.errorGetList.set(null);

    this.getAll().subscribe({
      next: (response: ApiResponseDTO<CreditCardResponseDTO[]>) => {
        const totalBalance = response.data?.reduce((sum, card) => sum + (card.currentBalance || 0), 0) || 0;
        creditCardState.creditCards.set(response.data || []);
        creditCardState.totalCreditBalance.set(totalBalance);
        creditCardState.isLoadingGetList.set(false);
      },
      error: (error) => {
        console.error('Error fetching credit cards:', error);
        creditCardState.errorGetList.set(error.message || 'Error fetching credit cards');
        creditCardState.isLoadingGetList.set(false);
      }
    })
  }
}
