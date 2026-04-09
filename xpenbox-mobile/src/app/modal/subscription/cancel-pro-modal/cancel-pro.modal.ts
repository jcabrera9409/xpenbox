import { Component, computed, output, signal } from '@angular/core';
import { subscriptionState } from '../../../feature/subscription/service/subscription.state';
import { DateService } from '../../../shared/service/date.service';
import { SubscriptionService } from '../../../feature/subscription/service/subscription.service';

@Component({
  selector: 'app-cancel-pro-modal',
  imports: [],
  templateUrl: './cancel-pro.modal.html',
  styleUrl: './cancel-pro.modal.css',
})
export class CancelProModal {

  subscriptionState = subscriptionState;
  
  close = output<void>();

  constructor(
    private subscriptionService: SubscriptionService,
    private dateService: DateService
  ) {}

  subscriptionEndDate = computed(() => {
    const subscription = subscriptionState.subscription();
    if (!subscription || !subscription.nextBillingDateTimestamp) return null;
    return this.dateService.format(subscription.nextBillingDateTimestamp, 'date');
  });

  closeCancelModal(): void {
    this.close.emit();
  }

  confirmCancelSubscription(): void {
    this.subscriptionState.isLoadingSending.set(true);
    this.subscriptionState.errorSending.set(null);

    this.subscriptionService.cancelSubscription().subscribe({
      next: (response) => {
        if (response.success) {
          this.subscriptionState.isLoadingSending.set(false);
          this.closeCancelModal();
          this.subscriptionService.loadUserSubscription();
        } else {
          this.subscriptionState.isLoadingSending.set(false);
          this.subscriptionState.errorSending.set(response.message || 'Error al cancelar la suscripción');
        }
      },
      error: (error) => {
        this.subscriptionState.isLoadingSending.set(false);
        this.subscriptionState.errorSending.set('Error al cancelar la suscripción. Por favor, inténtalo de nuevo.');
        console.error('Error al cancelar suscripción:', error);
      }
    });
  }
}
