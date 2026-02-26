import { Component, computed } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../feature/auth/service/auth.service';
import { userState } from '../../feature/user/service/user.state';
import { subscriptionState } from '../../feature/subscription/service/subscription.state';
import { SubscriptionResponseDTO } from '../../feature/subscription/model/subscription.response.dto';
import { upgradeProModalState } from '../../modal/subscription/state/upgrade-pro.modal.state';

@Component({
  selector: 'app-settings-page',
  imports: [],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.css',
})
export class SettingsPage {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  userState = userState;

  profileImageUrl = computed(() => {
    const email = userState.userLogged()?.email;
    if (!email) return null;
    
    const username = email.split('@')[0];
    return `https://api.dicebear.com/9.x/identicon/svg?seed=${username}`;
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
}
