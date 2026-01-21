import { Component, inject, PLATFORM_ID } from '@angular/core';
import { AccountCard } from '../../shared/cards/account-card/account.card';
import { CreditCard } from '../../shared/cards/credit-card/credit.card';
import { AccountService } from '../../feature/account/service/account.service';
import { CommonModule } from '@angular/common';
import { accountState } from '../../feature/account/service/account.state';
import { isPlatformServer } from '@angular/common';
import { CreditCardService } from '../../feature/creditcard/service/creditcard.service';
import { creditCardState } from '../../feature/creditcard/service/creditcard.state';

@Component({
  selector: 'app-account-page',
  imports: [AccountCard, CreditCard, CommonModule],
  templateUrl: './account.page.html',
  styleUrl: './account.page.css',
})
export class AccountPage {

  accountState = accountState;
  creditCardState = creditCardState;

  constructor(
    private accountService: AccountService,
    private creditCardService: CreditCardService
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.accountService.loadAccounts();
    this.creditCardService.loadCreditCards();
  }
  
}
