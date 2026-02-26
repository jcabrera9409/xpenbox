import { Component, signal, OnInit, effect, inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule, isPlatformBrowser, isPlatformServer } from '@angular/common';
import { SubscriptionService } from '../../../feature/subscription/service/subscription.service';
import { subscriptionState } from '../../../feature/subscription/service/subscription.state';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { SubscriptionResponseDTO, SubscriptionStatus } from '../../../feature/subscription/model/subscription.response.dto';

type PaymentStatus = 'confirmed' | 'pending' | 'loading';

@Component({
  selector: 'app-success-page',
  standalone: true,
  imports: [CommonModule, LoadingUi],
  templateUrl: './success.page.html',
  styleUrl: './success.page.css',
})
export class SuccessPage implements OnInit {

  private platformId = inject(PLATFORM_ID);
  private isBrowser = isPlatformBrowser(this.platformId);

  preapprovalId = signal<string | null>(null);
  paymentStatus = signal<PaymentStatus>('loading');
  isLoading = signal(false);
  redirectCountdown = signal(9);
  subscriptionState = subscriptionState;

  constructor(
    private subscriptionService: SubscriptionService,
    private route: ActivatedRoute
  ) {
    if (this.isBrowser) {
      effect(() => {
        const subscription: SubscriptionResponseDTO = this.subscriptionState.subscription()!;
        this.isLoading.set(false);
        if (subscription) {
          if (subscription.providerSubscriptionId === this.preapprovalId() && subscription.status === SubscriptionStatus.ACTIVE) {
            this.paymentStatus.set('confirmed');
            this.startRedirectCountdown();
          } else {
            this.paymentStatus.set('pending')
          }
        }
      });
    }
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const id = params['preapproval_id'];
      if (!id) {
        this.redirectToHome();
        return;
      }

      this.preapprovalId.set(id || null);
      this.waitForPaymentConfirmation();
    });
  }

  private waitForPaymentConfirmation(): void {
    if (!this.isBrowser) return;

    const interval = setInterval(() => {
      this.refreshSubscriptionData();
      clearInterval(interval);
    }, 5000);
  }

  private startRedirectCountdown(): void {
    const interval = setInterval(() => {
      const current = this.redirectCountdown();
      if (current <= 1) {
        clearInterval(interval);
        this.redirectToHome();
      } else {
        this.redirectCountdown.set(current - 1);
      }
    }, 1000);
  }

  redirectToHome(): void {
    window.location.href = '/landing';
  }

  refreshStatus(): void {
    if (!this.isBrowser) return;

    this.isLoading.set(true);
    
    this.refreshSubscriptionData();
  }

  private refreshSubscriptionData(): void {
    if (!this.isBrowser) return;
    this.subscriptionService.loadUserSubscription();
  }
}
