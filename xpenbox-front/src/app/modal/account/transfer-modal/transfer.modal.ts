import { Component, effect, input, OnInit, output, signal } from '@angular/core';
import { accountState } from '../../../feature/account/service/account.state';
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { AccountResponseDTO } from '../../../feature/account/model/account.response.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { AccountService } from '../../../feature/account/service/account.service';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { VirtualKeyboardUi } from '../../../shared/ui/virtual-keyboard-ui/virtual-keyboard.ui';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { AccountCreditDTO } from '../../../shared/dto/account-credit.dto';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';

@Component({
  selector: 'app-transfer-modal',
  imports: [CommonModule, LoadingUi, RetryComponent, VirtualKeyboardUi, ModalButtonsUi, AccountsCarouselComponent],
  templateUrl: './transfer.modal.html',
  styleUrl: './transfer.modal.css',
})
export class TransferModal implements OnInit {

  accountResourceCode = input<string | null>();
  close = output<void>();

  accountState = accountState;
  transactionState = transactionState;

  accountsList = signal<AccountCreditDTO[]>([]);
  selectedDestinationAccount = signal<AccountCreditDTO | null>(null);

  accountData = signal<AccountResponseDTO | null>(null);
  amount = signal(0);
  description = signal('');

  constructor(
    private transactionService: TransactionService,
    private accountService: AccountService,
    private accountCreditService: AccountCreditService
  ) { 
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }

    // Auto-select first account when loaded and sort accounts
    effect(() => {
      const selectedAccount = this.accountResourceCode();
      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);
      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, -1);
      // remove selected account from the list
      const newAccountsList = filteredAccounts.filter(acc => acc.resourceCode != selectedAccount);
      
      if (newAccountsList.length > 0) {
        this.accountsList.set(newAccountsList);
        
        if (!this.selectedDestinationAccount()) {
          this.selectedDestinationAccount.set(newAccountsList[0] || null);
        }
      }
    });
  }

  get isFormValid(): boolean {
    const amountValue = this.amount();
    const amountOriginAccount = this.accountData()?.balance || 0;
    const selectedAccount = this.selectedDestinationAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid && amountValue <= amountOriginAccount;
  }

  ngOnInit(): void {
    this.transactionState.isLoadingSendingTransaction.set(false);
    this.transactionState.errorSendingTransaction.set(null);
    
    this.loadAccountData();
  }

  retryLoadAccountData() {
    this.loadAccountData();
  }

  retryLoadAccountsData(): void {
    this.accountService.refresh();
  }

  onClose() {
    this.close.emit();
  }

  onSubmit() {
    if (!this.isFormValid) return;

    const amountValue = this.amount();
    const descriptionValue = this.description();
    const originAccountResourceCode = this.accountResourceCode() || '';
    const destinationAccountResourceCode = this.selectedDestinationAccount()?.resourceCode || '';

    const transactionRequest = TransactionRequestDTO.generateTransferTransaction(
      amountValue,
      descriptionValue,
      originAccountResourceCode,
      destinationAccountResourceCode
    );

    this.transactionState.isLoadingSendingTransaction.set(true);
    this.transactionState.errorSendingTransaction.set(null);

    this.transactionService.create(transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.transactionState.isLoadingSendingTransaction.set(false);

        if (response.success && response.data) {
          this.close.emit();
          this.transactionState.successSendingTransaction.set(true);

          this.accountService.refresh();
        }
      }, error: (error) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        console.error('Error creating income assignment:', error);
        if (error.status === 500 || error.status === 0) {
          this.transactionState.errorSendingTransaction.set('Error guardando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.errorSendingTransaction.set(error.error.message || 'Error guardando la transacción.');
        }
      }
    });
  }

  private loadAccountData(): void {
    this.accountState.isLoadingGetAccount.set(true);

    this.accountService.getByResourceCode(this.accountResourceCode()!).subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        if (response.success && response.data) {
          const accountData = response.data;
          this.accountData.set(accountData);
        }
        this.accountState.isLoadingGetAccount.set(false);
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.accountState.errorGetAccount.set('Error obteniendo los datos de la cuenta. Por favor, inténtalo de nuevo.');
        } else {
          this.accountState.errorGetAccount.set(error.error.message || 'Error obteniendo los datos de la cuenta.');
        }
      }
    });
  }
}
