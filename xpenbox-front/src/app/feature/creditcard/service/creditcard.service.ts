import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponseDTO } from '../../common/model/apiResponseDTO';
import { CreditCardResponseDTO } from '../model/creditCardResponseDTO';
import { creditCardState } from './creditcard.state';

@Injectable({
  providedIn: 'root',
})
export class CreditCardService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private envService: EnvService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/creditcard`;
  }

  getCreditCards(): Observable<ApiResponseDTO<CreditCardResponseDTO[]>> {
    return this.http.get<ApiResponseDTO<CreditCardResponseDTO[]>>(this.apiUrl, { withCredentials: true });
  }

  loadCreditCards(): void {
    creditCardState.isLoading.set(true);
    creditCardState.error.set(null);

    this.getCreditCards().subscribe({
      next: (response: ApiResponseDTO<CreditCardResponseDTO[]>) => {
        const totalBalance = response.data?.reduce((sum, card) => sum + (card.currentBalance || 0), 0) || 0;
        creditCardState.creditCards.set(response.data || []);
        creditCardState.totalCreditBalance.set(totalBalance);
        creditCardState.isLoading.set(false);
      },
      error: (error) => {
        console.error('Error fetching credit cards:', error);
        creditCardState.error.set(error.message || 'Error fetching credit cards');
        creditCardState.isLoading.set(false);
      }
    })
  }

  refreshCreditCards(): void {
    this.loadCreditCards();
  }
}
