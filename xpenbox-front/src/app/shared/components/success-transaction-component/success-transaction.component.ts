import { Component, effect, signal } from '@angular/core';
import { transactionState } from '../../../feature/transaction/service/transaction.state';

@Component({
  selector: 'app-success-transaction-component',
  imports: [],
  templateUrl: './success-transaction.component.html',
  styleUrl: './success-transaction.component.css',
})
export class SuccessTransactionComponent {

  transactionState = transactionState;

  visible = signal(false);
  fading = signal(false);

  constructor() {
    effect(() => {
      const isVisible = this.transactionState.isSuccess();
      if (isVisible) {
        this.showSuccess();
      }
    });
  }

  showSuccess(): void {
    this.visible.set(true);
    setTimeout(() => {
      this.fading.set(true);
      setTimeout(() => {
        this.fading.set(false);
        this.visible.set(false)
        this.transactionState.isSuccess.set(false);
      }, 600);
    }, 1500);
  }
}
