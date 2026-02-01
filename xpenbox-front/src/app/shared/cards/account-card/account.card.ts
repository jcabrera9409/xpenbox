import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { userState } from '../../../feature/user/service/user.state';

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

  userLogged = userState.userLogged;

  accountCardResourceCode = input<string>();
  accountCardName = input<string>();
  accountCardBalance = input<number>();
  accountCardState = input<boolean>();

  accountCardEdit = output<string>();
  accountCardTransfer = output<string>();

  constructor(
    private router: Router
  ) { }

  onEditAccount() {
    this.accountCardEdit.emit(this.accountCardResourceCode() || '');
  }

  onTransferFromAccount() {
    this.accountCardTransfer.emit(this.accountCardResourceCode() || '');
  }

  onViewTransactions() {
    this.router.navigate(['/landing/transaction'], { 
      queryParams: { 
        source: 'account', 
        code: this.accountCardResourceCode() 
      }});
  }

  onDisableAccount() {
    console.log('Disable account clicked for', this.accountCardResourceCode());
  }
}
