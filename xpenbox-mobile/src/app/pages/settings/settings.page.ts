import { Component, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../feature/auth/service/auth.service';
import { userState } from '../../feature/user/service/user.state';
import { subscriptionState } from '../../feature/subscription/service/subscription.state';
import { SubscriptionResponseDTO } from '../../feature/subscription/model/subscription.response.dto';
import { upgradeProModalState } from '../../modal/subscription/state/upgrade-pro.modal.state';
import { DateService } from '../../shared/service/date.service';
import { CancelProModal } from '../../modal/subscription/cancel-pro-modal/cancel-pro.modal';

@Component({
  selector: 'app-settings-page',
  imports: [CommonModule, CancelProModal],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.css',
})
export class SettingsPage {

  constructor(
    private authService: AuthService,
    private router: Router,
    private dateService: DateService
  ) {}

  userState = userState;
  subscriptionState = subscriptionState;

  showCancelModal = signal<boolean>(false);

  profileImageUrl = computed(() => {
    const email = userState.userLogged()?.email;
    if (!email) return null;
    
    const username = email.split('@')[0];
    return `https://api.dicebear.com/9.x/identicon/svg?seed=${username}`;
  });

  subscriptionEndDate = computed(() => {
    const subscription = subscriptionState.subscription();
    if (!subscription || !subscription.endDateTimestamp) return null;
    return this.dateService.format(subscription.endDateTimestamp, 'date');
  });

  subscriptionRenewDate = computed(() => {
    const subscription = subscriptionState.subscription();
    if (!subscription || !subscription.nextBillingDateTimestamp) return null;
    return this.dateService.format(subscription.nextBillingDateTimestamp, 'date');
  });

  get isPremiumUser(): boolean {
    const subscription: SubscriptionResponseDTO = subscriptionState.subscription()!; 
    if (subscription && subscription.plan.price > 0) return true;
    return false;
  }

  onClickPremium(): void {
    upgradeProModalState.title.set('Desbloquea todo el potencial de tu cuenta');
    upgradeProModalState.htmlMessage.set('Actualiza a Premium y accede a funciones avanzadas que te ayudarán a gestionar tus finanzas sin límites.');
    upgradeProModalState.open.set(true);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.error('Error al cerrar sesión:', error);
      }
    });
  }

  openCancelModal(): void {
    this.showCancelModal.set(true);
  }

  closeCancelModal(): void {
    this.showCancelModal.set(false);
  }
}
