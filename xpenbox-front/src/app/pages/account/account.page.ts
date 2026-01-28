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
import { CreditcardEditionModal } from '../../modal/account/creditcard-edition-modal/creditcard-edition.modal';
import { SummaryCard } from '../../shared/cards/summary-card/summary.card';
import { LoadingUi } from '../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../shared/components/retry-component/retry.component';
import { CreateFirstComponent } from '../../shared/components/create-first-component/create-first.component';

@Component({
  selector: 'app-account-page',
  imports: [AccountCard, CreditCard, CommonModule, AccountEditionModal, CreditcardEditionModal, SummaryCard, LoadingUi, RetryComponent, CreateFirstComponent],
  templateUrl: './account.page.html',
  styleUrl: './account.page.css',
})
export class AccountPage {

  accountState = accountState;
  creditCardState = creditCardState;

  showAccountEditionModal = signal(false);
  resourceCodeAccountSelected = signal<string | null>(null);

  showCreditCardEditionModal = signal(false);
  resourceCodeCreditCardSelected = signal<string | null>(null);

  constructor(
    private accountService: AccountService,
    private creditCardService: CreditCardService
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.accountService.load();
    this.creditCardService.load();
  }

  openAccountEditionModal(resourceCodeAccountSelected: string | null = null) {
    this.resourceCodeAccountSelected.set(resourceCodeAccountSelected);
    this.showAccountEditionModal.set(true);
  }

  closeAccountEditionModal() {
    this.showAccountEditionModal.set(false);
  }

  reloadAccounts() {
    this.accountService.refresh();
  }
  
  openCreditCardEditionModal(resourceCodeCreditCardSelected: string | null = null) {
    this.resourceCodeCreditCardSelected.set(resourceCodeCreditCardSelected);
    this.showCreditCardEditionModal.set(true);
  }

  closeCreditCardEditionModal() {
    this.showCreditCardEditionModal.set(false);
  }

  reloadCreditCards() {
    this.creditCardService.refresh();
  }
}
