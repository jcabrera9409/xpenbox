import { Component, output, input, signal, OnInit, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountCreditDTO, AccountCreditType } from '../../../shared/dto/account-credit.dto';
import { AccountService } from '../../../feature/account/service/account.service';
import { accountState } from '../../../feature/account/service/account.state';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { CreditCardResponseDTO } from '../../../feature/creditcard/model/creditcard.response.dto';
import { AccountResponseDTO } from '../../../feature/account/model/account.response.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { userState } from '../../../feature/user/service/user.state';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { genericState } from '../../../feature/common/service/generic.state';
import { AccountDeactivateRequestDTO } from '../../../feature/account/model/account.deactivate.request.dto';
import { CreditCardDeactivateRequestDTO } from '../../../feature/creditcard/model/creditcard.deactivate.request.dto';

@Component({
  selector: 'app-disable-account-credit-modal',
  imports: [CommonModule, LoadingUi, RetryComponent, AccountsCarouselComponent],
  templateUrl: './disable-account-credit.modal.html',
  styleUrl: './disable-account-credit.modal.css',
})
export class DisableAccountCreditModal implements OnInit {
  userLogged = userState.userLogged;

  // Inputs
  resourceCode = input<string | null>();
  resourceType = input<AccountCreditType | null>();

  accountState = accountState;
  creditCardState = creditCardState;
  transactionState = transactionState;
  genericState = genericState;

  creditCardData = signal<CreditCardResponseDTO | null>(null);
  accountData = signal<AccountResponseDTO | null>(null);
  currentBalance = signal<number>(0);

  accountList = signal<AccountCreditDTO[]>([]);
  selectedAccount = signal<AccountCreditDTO | null>(null);

  // State
  userConfirmed = signal(false);
  
  // Outputs
  close = output<void>();

  constructor(
    private accountService: AccountService,
    private creditCardService: CreditCardService,
    private accountCreditService: AccountCreditService
  ) {
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }
    
    effect(() => {
      if (this.currentBalance() === 0) return;

      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);
      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, -1);

      if (this.resourceType() === AccountCreditType.CREDIT_CARD) {
        this.accountList.set(
          filteredAccounts.filter(ac => ac.balance > this.currentBalance())
        );
      } else {
        this.accountList.set(
          filteredAccounts.filter(ac => ac.resourceCode !== this.resourceCode())
        );
      }

      if (this.accountList().length > 0 && !this.selectedAccount()) {
        this.selectedAccount.set(this.accountList()[0] || null);
      }

    });

  }

  ngOnInit(): void {
    this.accountState.isLoadingGetAccount.set(false);
    this.accountState.errorGetAccount.set(null);
    this.accountState.isLoadingSendingAccount.set(false);
    this.accountState.errorSendingAccount.set(null);
    this.creditCardState.isLoadingGetCreditCard.set(false);
    this.creditCardState.errorGetCreditCard.set(null);
    this.creditCardState.isLoadingSendingCreditCard.set(false);
    this.creditCardState.errorSendingCreditCard.set(null);

    this.loadResourceData();
  }

  // Computed
  get hasBalance(): boolean {
    return this.currentBalance() !== 0;
  }

  get balanceMessage(): string {
    const balance = this.currentBalance();
    const type = this.resourceType();
    
    if (balance === 0) return '';
    
    if (type === AccountCreditType.CREDIT_CARD) {
      return `Tiene un saldo pendiente de pagar de ${this.userLogged()?.currency || ''} ${Math.abs(balance).toFixed(2)}. Debe cancelar el saldo antes de desactivar esta tarjeta de crédito.`;
    } else {
      return `Tiene un saldo disponible de ${this.userLogged()?.currency || ''} ${Math.abs(balance).toFixed(2)}. Debe transferir el saldo antes de desactivar esta cuenta.`;
    }
  }

  get resourceTypeLabel(): string {
    return this.resourceType() === AccountCreditType.CREDIT_CARD ? 'tarjeta de crédito' : 'cuenta';
  }

  get isCreditCard(): boolean {
    return this.resourceType() === AccountCreditType.CREDIT_CARD;
  }

  get isFormValid(): boolean {
    if (this.hasBalance) {
      return this.selectedAccount() !== null && this.userConfirmed();
    } else {
      return this.userConfirmed();
    }
  }

  get resourceName(): string {
    if (this.resourceType() === AccountCreditType.CREDIT_CARD) {
      return this.creditCardData()?.name || '';
    } else {
      return this.accountData()?.name || '';
    }
  }

  onClose(): void {
    this.close.emit();
  }

  onConfirm(): void {
    if (!this.isFormValid) return;
    
    if (this.isCreditCard) {
      this.sendDisableCreditCard();
    } else {
      this.sendDisableAccount();
    }
  }

  private sendDisableCreditCard(): void {
    this.creditCardState.isLoadingSendingCreditCard.set(true);

    const accountDeactivateRequestDTO = new CreditCardDeactivateRequestDTO(
      this.hasBalance ? this.selectedAccount()!.resourceCode : undefined
    );

    this.creditCardService.deactivateCreditCard(this.resourceCode()!, accountDeactivateRequestDTO).subscribe({
      next: () => {
        this.creditCardService.refresh();

        if (this.hasBalance) {
          this.accountService.refresh();
        }

        const paymentMessage = this.hasBalance ? `Se pagaron ${this.userLogged()?.currency || ''} ${Math.abs(this.currentBalance()).toFixed(2)} desde la cuenta "${this.selectedAccount()!.name}".` : '';
        this.creditCardState.isLoadingSendingCreditCard.set(false);
        this.genericState.showReceiptModal.set(true);
        this.genericState.contentReceiptModal.set(`Tarjeta de crédito "${this.resourceName}" desactivada. ${paymentMessage}`);
        
        this.close.emit();
      }, error: (error) => {
        this.creditCardState.isLoadingSendingCreditCard.set(false);
        if (error.status === 500 || error.status === 0) {
          this.creditCardState.errorSendingCreditCard.set('Error al desactivar la tarjeta de crédito. Intente nuevamente.');
        } else {
          this.creditCardState.errorSendingCreditCard.set(error.error?.message || 'Error desconocido al desactivar la tarjeta de crédito.');
        }
      }
    });
  }

  private sendDisableAccount(): void {
    this.accountState.isLoadingSendingAccount.set(true);

    const accountDeactivateRequestDTO = new AccountDeactivateRequestDTO(
      this.hasBalance ? this.selectedAccount()!.resourceCode : undefined
    );

    this.accountService.deactivateAccount(this.resourceCode()!, accountDeactivateRequestDTO).subscribe({
      next: () => {
        this.accountService.refresh();

        const transferMessage = this.hasBalance ? `Se transfirieron ${this.userLogged()?.currency || ''} ${Math.abs(this.currentBalance()).toFixed(2)} a la cuenta "${this.selectedAccount()!.name}".` : '';
        this.accountState.isLoadingSendingAccount.set(false);
        this.genericState.showReceiptModal.set(true);
        this.genericState.contentReceiptModal.set(`Cuenta "${this.resourceName}" desactivada. ${transferMessage}`);

        this.close.emit();
      }, error: (error) => {
        this.accountState.isLoadingSendingAccount.set(false);
        if (error.status === 500 || error.status === 0) {
          this.accountState.errorSendingAccount.set('Error al desactivar la cuenta. Intente nuevamente.');
        } else {
          this.accountState.errorSendingAccount.set(error.error?.message || 'Error desconocido al desactivar la cuenta.');
        }
      }
    });
  }
  
  toggleConfirmation(): void {
    this.userConfirmed.set(!this.userConfirmed());
  }

  retryLoadResourceData(): void {
    this.loadResourceData();
  }

  retryLoadAccountData(): void {
    this.accountService.refresh();
  }

  private loadResourceData(): void {
    const type = this.resourceType();

    if (type == AccountCreditType.CREDIT_CARD) {
      this.loadCreditCardData();
    } else {
      this.loadAccountData();
    }
  }

  private loadCreditCardData(): void {
    this.creditCardState.isLoadingGetCreditCard.set(true);

    this.creditCardService.getByResourceCode(this.resourceCode()!).subscribe({
      next: (response: ApiResponseDTO<CreditCardResponseDTO>) => {
        this.creditCardState.isLoadingGetCreditCard.set(false);
        if (response.success && response.data) {
          this.creditCardData.set(response.data);
          this.currentBalance.set(response.data.currentBalance);
        }
      },
      error: (error) => {
        this.creditCardState.isLoadingGetCreditCard.set(false);
        if (error.status === 500 || error.status === 0) {
          this.creditCardState.errorGetCreditCard.set('Error al cargar la información de la tarjeta de crédito. Intente nuevamente.');
        } else {
          this.creditCardState.errorGetCreditCard.set(error.error?.message || 'Error desconocido al cargar la información de la tarjeta de crédito.');
        }
      }
    });
  }

  private loadAccountData(): void {
    this.accountState.isLoadingGetAccount.set(true);

    this.accountService.getByResourceCode(this.resourceCode()!).subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        this.accountState.isLoadingGetAccount.set(false);
        if (response.success && response.data) {
          this.accountData.set(response.data);
          this.currentBalance.set(response.data.balance);
        }
      },
      error: (error) => {
        this.accountState.isLoadingGetAccount.set(false);
        if (error.status === 500 || error.status === 0) {
          this.accountState.errorGetAccount.set('Error al cargar la información de la cuenta. Intente nuevamente.');
        } else {
          this.accountState.errorGetAccount.set(error.error?.message || 'Error desconocido al cargar la información de la cuenta.');
        }
      }
    });
  }
}
