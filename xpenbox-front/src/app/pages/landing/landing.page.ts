import { Component, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { MenuComponent } from '../../shared/components/menu-component/menu.component';
import { RouterOutlet } from '@angular/router';
import { QuickExpenseModal } from '../../modal/transaction/quick-expense-modal/quick-expense.modal';
import { CommonModule } from '@angular/common';
import { UserService } from '../../feature/user/service/user.service';
import { userState } from '../../feature/user/service/user.state';

@Component({
  selector: 'app-landing-page',
  imports: [MenuComponent, RouterOutlet, QuickExpenseModal, CommonModule],
  templateUrl: './landing.page.html',
  styleUrl: './landing.page.css',
})
export class LandingPage {
  
  showQuickExpenseModal = signal(false);

  private userState = userState;
  private platformId = inject(PLATFORM_ID);

  constructor(
    private userService: UserService
  ) {
    if (isPlatformBrowser(this.platformId) && !this.userState.userLogged()) {
      this.userService.loadUserLoggedIn();
    }
  }

  openQuickExpenseModal() {
    this.showQuickExpenseModal.set(true);
  }

  closeQuickExpenseModal() {
    this.showQuickExpenseModal.set(false);
  }
}
