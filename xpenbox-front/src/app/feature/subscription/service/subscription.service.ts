import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EnvService } from '../../common/service/env.service';
import { Observable, tap } from 'rxjs';
import { ApiResponseDTO } from '../../common/model/api.response.dto';
import { SubscriptionResponseDTO } from '../model/subscription.response.dto';
import { subscriptionState } from './subscription.state';
import { PreApprovalSubscriptionResponseDTO } from '../model/pre-approval-subscription.response.dto';
import { PreApprovalSubscriptionRequestDTO } from '../model/pre-approval-subscription.request.dto';

/**
 * Service for managing user subscriptions, including fetching subscription details and canceling subscriptions.
 */
@Injectable({
  providedIn: 'root',
})
export class SubscriptionService {

  private apiUrl: string;

  constructor(
    private http: HttpClient,
    private envService: EnvService
  ) {
    this.apiUrl = `${this.envService.getApiUrl()}/subscription`;
  }

  /**
   * Retrieves the current user's subscription details.
   * @returns An Observable containing the API response with subscription details.
   */
  getMySubscription(): Observable<ApiResponseDTO<SubscriptionResponseDTO>> {
    subscriptionState.isLoading.set(true);
    return this.http.get<ApiResponseDTO<SubscriptionResponseDTO>>(`${this.apiUrl}/me`,{ 
      withCredentials: true 
    }).pipe(
      tap((response: ApiResponseDTO<SubscriptionResponseDTO>) => {
        subscriptionState.isLoading.set(false);
        subscriptionState.subscription.set(response.data);
      })
    )
  }

  /**
   * Creates a pre-approval subscription for the user based on the provided request body.
   * @param requestBody - The request body containing the subscription plan and payment provider information.
   * @returns An Observable containing the API response with the pre-approval subscription details, including the URL for completing the subscription process.
   */
  createPreApprovalSubscription(requestBody: PreApprovalSubscriptionRequestDTO): Observable<ApiResponseDTO<PreApprovalSubscriptionResponseDTO>> {
    return this.http.post<ApiResponseDTO<PreApprovalSubscriptionResponseDTO>>(`${this.apiUrl}/pre-approval`, requestBody, {
      withCredentials: true
    });
  }

  /**
   * Cancels the current user's subscription.
   * @returns An Observable containing the API response for the cancellation request.
   */
  cancelSubscription(): Observable<ApiResponseDTO<void>> {
    return this.http.post<ApiResponseDTO<void>>(`${this.apiUrl}/cancel`, {});
  }

  loadUserSubscription(): void {
    this.getMySubscription().subscribe({
      next: (response: ApiResponseDTO<SubscriptionResponseDTO>) => {
        if (response.success) {
          subscriptionState.subscription.set(response.data);
        } else {
          subscriptionState.error.set(response.message);
        }
      },
      error: (error) => {
        subscriptionState.error.set('Error fetching subscription data.');
        console.error('Error fetching subscription data:', error);
      }
    });
  }
}
