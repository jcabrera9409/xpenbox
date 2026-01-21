import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-account-card',
  imports: [CommonModule],
  templateUrl: './account.card.html',
  styleUrl: './account.card.css',
  host: {
    'class': 'block',
    'ngSkipHydration': 'true'
  }
})
export class AccountCard {

  accountCardResourceCode = input<string>();
  accountCardName = input<string>();
  accountCardBalance = input<number>();
  accountCardState = input<boolean>();

  onEditAccount() {
    console.log('Edit account clicked for', this.accountCardResourceCode());
  }

  onViewTransactions() {
    console.log('View transactions clicked for', this.accountCardResourceCode());
  }

  onDisableAccount() {
    console.log('Disable account clicked for', this.accountCardResourceCode());
  }
}
