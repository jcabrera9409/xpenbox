import { Component, effect, input, OnInit, output, signal } from '@angular/core';
import { accountState } from '../../../feature/account/service/account.state';
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { AccountResponseDTO } from '../../../feature/account/model/account.response.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { AccountService } from '../../../feature/account/service/account.service';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { AccountCreditDTO, AccountCreditType } from '../../../shared/dto/account-credit.dto';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { DateService } from '../../../shared/service/date.service';
import { userState } from '../../../feature/user/service/user.state';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';
import { ModalGeneric } from '../../common/modal.generic';
import { InputComponent } from '../../../shared/components/input-component/input.component';
import { InputAmountComponent } from '../../../shared/components/input-amount-component/input-amount-component';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-transfer-modal',
  imports: [CommonModule, LoadingUi, RetryComponent, ModalButtonsUi, AccountsCarouselComponent, IconComponent, InputComponent, InputAmountComponent, ReactiveFormsModule],
  templateUrl: './transfer.modal.html',
  styleUrl: './transfer.modal.css',
})
export class TransferModal extends ModalGeneric implements OnInit {

  userLogged = userState.userLogged;

  accountResourceCode = input<string | null>();
  close = output<void>();

  accountState = accountState;
  transactionState = transactionState;

  accountsOriginList = signal<AccountCreditDTO[]>([]);
  accountsDestinationList = signal<AccountCreditDTO[]>([]);
  selectedDestinationAccount = signal<AccountCreditDTO | null>(null);
  selectedOriginAccount = signal<AccountCreditDTO | null>(null);

  formTransfer!: FormGroup;
  maxDate = signal('');

  constructor(
    private fb: FormBuilder,
    private transactionService: TransactionService,
    private accountService: AccountService,
    private accountCreditService: AccountCreditService,
    private dateService: DateService
  ) { 
    super();

    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }

    // Auto-select first account when loaded and sort accounts
    effect(() => {
      if (this.accountResourceCode()) return;

      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);
      const filteredOriginAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, 0);
      this.accountsOriginList.set(filteredOriginAccounts);
      if (this.accountsOriginList().length > 0) {
        this.selectedOriginAccount.set(this.accountsOriginList()[0] || null);
      }
    });

    // Update destination accounts list when origin account changes
    effect(() => {
      const originAccount = this.selectedOriginAccount();
      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);
      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, -1);
      // remove selected origin account from the list
      const newAccountsList = filteredAccounts.filter(acc => acc.resourceCode != originAccount?.resourceCode);
      this.accountsDestinationList.set(newAccountsList);

      // if the selected destination account is the same as the origin, reset it
      if (this.accountsDestinationList().length > 0) {
        this.selectedDestinationAccount.set(this.accountsDestinationList()[0] || null);
      }
    });
  }

  get isFormValid(): boolean {
    const amountValue = this.amountControl.value;
    const amountOriginAccount = this.selectedOriginAccount()?.balance || 0;
    const selectedAccount = this.selectedDestinationAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid && amountValue <= amountOriginAccount;
  }

  get amountControl(): FormControl {
    return this.formTransfer.get('amount') as FormControl;
  }

  get descriptionControl(): FormControl {
    return this.formTransfer.get('description') as FormControl;
  }

  get transactionDateControl(): FormControl {
    return this.formTransfer.get('transactionDate') as FormControl;
  }

  initForms(): void {
    const today = this.dateService.toTimestamp(this.dateService.getLocalDatetime());
    const formattedDate = this.dateService.format(today, 'ISO-LOCAL');
    this.maxDate.set(formattedDate);

    this.formTransfer = this.fb.group({
      amount: [null, [Validators.required, Validators.min(0.01)]],
      description: [''],
      transactionDate: [formattedDate, [Validators.required]]
    });
  }

  override ngOnInit(): void {
    super.ngOnInit();

    this.transactionState.isLoadingSendingTransaction.set(false);
    this.transactionState.errorSendingTransaction.set(null);
    
    if (this.accountResourceCode()) {
      this.loadAccountData();
    }

    this.initForms();
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

    const amountValue = this.amountControl.value / 100;
    const descriptionValue = this.descriptionControl.value;
    const originAccountResourceCode = this.selectedOriginAccount()?.resourceCode || '';
    const destinationAccountResourceCode = this.selectedDestinationAccount()?.resourceCode || '';
    const transactionDate = this.dateService.parseDatetimeIsoString(this.transactionDateControl.value);
    const transactionDateTimestamp = this.dateService.toTimestamp(transactionDate);

    const transactionRequest = TransactionRequestDTO.generateTransferTransaction(
      amountValue,
      descriptionValue,
      originAccountResourceCode,
      destinationAccountResourceCode,
      transactionDateTimestamp
    );

    this.transactionService.submitTransaction(transactionRequest, () => this.successTransactionCreated());
  }

  private successTransactionCreated(): void {
    this.close.emit();
    this.accountService.refresh();
    this.transactionService.refresh();
  }

  private loadAccountData(): void {
    this.accountState.isLoadingGetAccount.set(true);

    this.accountService.getByResourceCode(this.accountResourceCode()!).subscribe({
      next: (response: ApiResponseDTO<AccountResponseDTO>) => {
        if (response.success && response.data) {
          const accountData = response.data;
          const accountDTO: AccountCreditDTO = {
            resourceCode: accountData.resourceCode,
            type: AccountCreditType.ACCOUNT,
            icon: 'account_balance',
            name: accountData.name,
            balance: accountData.balance,
            lastUsedDateTimestamp: accountData.lastUsedDateTimestamp,
            usageCount: accountData.usageCount
          }
          this.selectedOriginAccount.set(accountDTO);
        }
        this.accountState.isLoadingGetAccount.set(false);
      },
      error: (error) => {
        this.accountState.isLoadingGetAccount.set(false);
        if (error.status === 500 || error.status === 0) {
          this.accountState.errorGetAccount.set('Error obteniendo los datos de la cuenta. Por favor, inténtalo de nuevo.');
        } else {
          this.accountState.errorGetAccount.set(error.error.message || 'Error obteniendo los datos de la cuenta.');
        }
      }
    });
  }
}
