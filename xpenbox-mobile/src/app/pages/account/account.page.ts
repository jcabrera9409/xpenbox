import { Component, computed, inject, PLATFORM_ID, signal } from '@angular/core';
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
import { TransferModal } from '../../modal/account/transfer-modal/transfer.modal';
import { CreditcardPaymentModal } from '../../modal/account/creditcard-payment-modal/creditcard-payment.modal';
import { userState } from '../../feature/user/service/user.state';
import { DisableAccountCreditModal } from '../../modal/account/disable-account-credit-modal/disable-account-credit.modal';
import { AccountCreditType } from '../../shared/dto/account-credit.dto';

@Component({
  selector: 'app-account-page',
  imports: [AccountCard, CreditCard, CommonModule, AccountEditionModal, CreditcardEditionModal, SummaryCard, LoadingUi, RetryComponent, CreateFirstComponent, TransferModal, CreditcardPaymentModal, DisableAccountCreditModal],
  templateUrl: './account.page.html',
  styleUrl: './account.page.css',
})
export class AccountPage {

  userLogged = userState.userLogged;

  accountState = accountState;
  creditCardState = creditCardState;

  showAccountEditionModal = signal(false);
  resourceCodeAccountSelected = signal<string | null>(null);

  showCreditCardEditionModal = signal(false);
  resourceCodeCreditCardSelected = signal<string | null>(null);

  showTransferModal = signal(false);
  resourceCodeTransferSelected = signal<string | null>(null);

  showDisableAccountCreditModal = signal(false);
  resourceCodeAccountCreditSelected = signal<string | null>(null);
  accountCreditTypeSelected = signal<AccountCreditType | null>(null);

  showCreditcardPaymentModal = signal(false);
  resourceCodeCreditcardPaymentSelected = signal<string | null>(null);

  // Computed signal for ordered accounts by name
  orderedAccounts = computed(() => {
    const accounts = accountState.accounts();
    return accounts.slice().sort((a, b) => a.name.localeCompare(b.name));
  })

  // Computed signal for ordered credit cards by name
  orderedCreditCards = computed(() => {
    const creditCards = creditCardState.creditCards();
    return creditCards.slice().sort((a, b) => a.name.localeCompare(b.name));
  })

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

  openTransferModal(resourceCodeTransferSelected: string | null = null) {
    this.resourceCodeTransferSelected.set(resourceCodeTransferSelected);
    this.showTransferModal.set(true);
  }

  closeTransferModal() {
    this.showTransferModal.set(false);
  }

  openDisableAccountModal(resourceCodeAccountSelected: string) {
    this.resourceCodeAccountCreditSelected.set(resourceCodeAccountSelected);
    this.accountCreditTypeSelected.set(AccountCreditType.ACCOUNT);
    this.showDisableAccountCreditModal.set(true);
  }

  openDisableCreditCardModal(resourceCodeCreditCardSelected: string) {
    this.resourceCodeAccountCreditSelected.set(resourceCodeCreditCardSelected);
    this.accountCreditTypeSelected.set(AccountCreditType.CREDIT_CARD);
    this.showDisableAccountCreditModal.set(true);
  }

  closeDisableAccountCreditModal() {
    this.showDisableAccountCreditModal.set(false);
  }

  reloadCreditCards() {
    this.creditCardService.refresh();
  }

  openCreditcardPaymentModal(resourceCodeCreditcardPaymentSelected: string | null = null) {
    this.resourceCodeCreditcardPaymentSelected.set(resourceCodeCreditcardPaymentSelected);
    this.showCreditcardPaymentModal.set(true);
  }

  closeCreditcardPaymentModal() {
    this.showCreditcardPaymentModal.set(false);
  }
}