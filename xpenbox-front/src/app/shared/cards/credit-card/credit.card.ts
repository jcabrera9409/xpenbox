import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-credit-card',
  imports: [CommonModule],
  templateUrl: './credit.card.html',
  styleUrl: './credit.card.css',
  host: {
    'class': 'block',
    'ngSkipHydration': 'true'
  }
})
export class CreditCard {
  
  creditCardResourceCode = input<string>();
  creditCardName = input<string>();
  creditCardLimit = input<number>();
  creditCardBalance = input<number>();
  creditCardState = input<boolean>();
  creditCardBillingDay = input<number>();
  creditCardPaymentDay = input<number>();

  creditCardEdit = output<string>();

  get creditCardBalancePercentage(): number {
    if (this.creditCardLimit() === 0 || this.creditCardBalance() === 0) return 0;

    return ((this.creditCardLimit() || 0) - (this.creditCardBalance() || 0)) / (this.creditCardLimit() || 0) * 100;
  }

  get creditCardBillingDate(): Date | null {
    const day = this.creditCardBillingDay();
    if (!day) return null;
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), day);
  }

  get creditCardPaymentDate(): Date | null {
    const day = this.creditCardPaymentDay();
    if (!day) return null;
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), day);
  }

  onEditCreditCard() {
    this.creditCardEdit.emit(this.creditCardResourceCode() || '');
  }

  onViewTransactions() {
    console.log('View transactions clicked for', this.creditCardResourceCode());
  }

  onDisableCreditCard() {
    console.log('Disable credit card clicked for', this.creditCardResourceCode());
  }
}
