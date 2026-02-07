import { Component, signal, inject, PLATFORM_ID, OnInit } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { MenuComponent } from '../../shared/components/menu-component/menu.component';
import { RouterOutlet } from '@angular/router';
import { QuickExpenseModal } from '../../modal/transaction/quick-expense-modal/quick-expense.modal';
import { CommonModule } from '@angular/common';
import { UserService } from '../../feature/user/service/user.service';
import { userState } from '../../feature/user/service/user.state';
import { SuccessTransactionComponent } from '../../shared/components/success-transaction-component/success-transaction.component';
import { IncomeEditionModal } from '../../modal/income/income-edition-modal/income-edition.modal';
import { CreditcardPaymentModal } from '../../modal/account/creditcard-payment-modal/creditcard-payment.modal';
import { TransferModal } from '../../modal/account/transfer-modal/transfer.modal';
import { ReceiptModal } from '../../modal/common/receipt-modal/receipt.modal';

@Component({
  selector: 'app-landing-page',
  imports: [MenuComponent, RouterOutlet, QuickExpenseModal, CommonModule, SuccessTransactionComponent, IncomeEditionModal, CreditcardPaymentModal, TransferModal, ReceiptModal],
  templateUrl: './landing.page.html',
  styleUrl: './landing.page.css',
})
export class LandingPage implements OnInit {
  
  showQuickExpenseModal = signal(false);
  showQuickIncomeModal = signal(false);
  showQuickTransferModal = signal(false);
  showQuickCreditCardPaymentModal = signal(false);

  private userState = userState;
  private platformId = inject(PLATFORM_ID);

  constructor(
    private userService: UserService
  ) {}

  ngOnInit(): void {
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
  }

  closeQuickIncomeModal() {
    this.showQuickIncomeModal.set(false);
  }

  openQuickTransferModal() {
    this.showQuickTransferModal.set(true);
  }

  closeQuickTransferModal() {
    this.showQuickTransferModal.set(false);
  }

  openQuickCreditCardPaymentModal() {
    this.showQuickCreditCardPaymentModal.set(true);
  }

  closeQuickCreditCardPaymentModal() {
    this.showQuickCreditCardPaymentModal.set(false);
  }
}
