import { Component, input } from '@angular/core';

@Component({
  selector: 'app-account-card',
  imports: [],
  templateUrl: './account.card.html',
  styleUrl: './account.card.css',
  host: {
    'class': 'block',
    'ngSkipHydration': 'true'
  }
})
export class AccountCard {

  accountCardResourceCode = input<string>();
  accountCardName = input<string>("hola");
  accountCardBalance = input<number>(1000.10);
  accountCardState = input<boolean>(true);

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
