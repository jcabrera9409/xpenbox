import { Component, inject, PLATFORM_ID, signal } from '@angular/core';
import { AccountCard } from '../../shared/cards/account-card/account.card';
import { CreditCard } from '../../shared/cards/credit-card/credit.card';
import { AccountService } from '../../feature/account/service/account.service';
import { CommonModule } from '@angular/common';
import { accountState } from '../../feature/account/service/account.state';
import { isPlatformServer } from '@angular/common';
import { CreditCardService } from '../../feature/creditcard/service/creditcard.service';
import { creditCardState } from '../../feature/creditcard/service/creditcard.state';
import { AccountEditionModal } from '../../modal/account/account-edition-modal/account-edition.modal';

@Component({
  selector: 'app-account-page',
  imports: [AccountCard, CreditCard, CommonModule, AccountEditionModal],
  templateUrl: './account.page.html',
  styleUrl: './account.page.css',
})
export class AccountPage {

  accountState = accountState;
  creditCardState = creditCardState;

  showAccountEditionModal = signal(false);
  resourceCodeSelected = signal<string | null>(null);

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

  openAccountEditionModal(resourceCodeSelected: string | null = null) {
    this.resourceCodeSelected.set(resourceCodeSelected);
    this.showAccountEditionModal.set(true);
  }

  closeAccountEditionModal() {
    this.showAccountEditionModal.set(false);
  }
  
}
