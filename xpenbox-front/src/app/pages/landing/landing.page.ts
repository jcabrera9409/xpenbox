import { Component, signal, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { MenuComponent } from '../../shared/components/menu-component/menu.component';
import { RouterOutlet } from '@angular/router';
import { QuickExpenseModal } from '../../modal/transaction/quick-expense-modal/quick-expense.modal';
import { CommonModule } from '@angular/common';
import { UserService } from '../../feature/user/service/user.service';
import { userState } from '../../feature/user/service/user.state';
import { SuccessTransactionComponent } from '../../shared/components/success-transaction-component/success-transaction.component';

@Component({
  selector: 'app-landing-page',
  imports: [MenuComponent, RouterOutlet, QuickExpenseModal, CommonModule, SuccessTransactionComponent],
  templateUrl: './landing.page.html',
  styleUrl: './landing.page.css',
})
export class LandingPage {
  
  showQuickExpenseModal = signal(false);
  showQuickIncomeModal = signal(false);
  showQuickTransferModal = signal(false);
  showQuickCreditCardPaymentModal = signal(false);

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

  openQuickIncomeModal() {
    this.showQuickIncomeModal.set(true);
    // TODO: Implementar modal de ingreso rápido
    console.log('Abrir modal de ingreso rápido');
  }

  closeQuickIncomeModal() {
    this.showQuickIncomeModal.set(false);
  }

  openQuickTransferModal() {
    this.showQuickTransferModal.set(true);
    // TODO: Implementar modal de transferencia rápida
    console.log('Abrir modal de transferencia rápida');
  }

  closeQuickTransferModal() {
    this.showQuickTransferModal.set(false);
  }

  openQuickCreditCardPaymentModal() {
    this.showQuickCreditCardPaymentModal.set(true);
    // TODO: Implementar modal de pago de tarjeta
    console.log('Abrir modal de pago de tarjeta');
  }

  closeQuickCreditCardPaymentModal() {
    this.showQuickCreditCardPaymentModal.set(false);
  }
}
