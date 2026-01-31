import { Component, effect, input, output, signal } from '@angular/core';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { AccountCreditDTO } from '../../../shared/dto/account-credit.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { AccountService } from '../../../feature/account/service/account.service';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { accountState } from '../../../feature/account/service/account.state';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { CreditCardResponseDTO } from '../../../feature/creditcard/model/creditcard.response.dto';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { VirtualKeyboardUi } from '../../../shared/ui/virtual-keyboard-ui/virtual-keyboard.ui';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { DateService } from '../../../shared/service/date.service';
import { CategoriesCarouselComponent } from '../../../shared/components/categories-carousel-component/categories-carousel.component';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';

@Component({
  selector: 'app-creditcard-payment-modal',
  imports: [CommonModule, LoadingUi, RetryComponent, VirtualKeyboardUi, ModalButtonsUi, AccountsCarouselComponent, CategoriesCarouselComponent],
  templateUrl: './creditcard-payment.modal.html',
  styleUrl: './creditcard-payment.modal.css',
})
export class CreditcardPaymentModal {

  creditCardResourceCode = input<string | null>();
  close = output<void>();

  accountState = accountState;
  creditCardState = creditCardState;
  transactionState = transactionState;

  accountsList = signal<AccountCreditDTO[]>([]);
  selectedAccount = signal<AccountCreditDTO | null>(null);
  selectedCategory = signal<CategoryResponseDTO | null>(null);
  assignToCategory = signal<boolean>(false);

  creditCardData = signal<CreditCardResponseDTO | null>(null);
  amount = signal(0);
  description = signal('');

  constructor(
    private transactionService: TransactionService,
    private accountService: AccountService,
    private creditCardService: CreditCardService,
    private accountCreditService: AccountCreditService,
    private dateService: DateService
  ) { 
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }

    // Auto-select first account when loaded and sort accounts
    effect(() => {
      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);

      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, this.amount());
      this.accountsList.set(filteredAccounts);

      if (filteredAccounts.length > 0 && !this.selectedAccount()) {
          this.selectedAccount.set(filteredAccounts[0] || null);
      }
    });

    // Update selected account when amount changes
    effect(() => {
      const amountValue = this.amount();
      const accounts = this.accountsList();
      const currentSelected = this.selectedAccount();
      
      // If amount is invalid, reset to first account
      if (isNaN(amountValue) || amountValue <= 0) {
        if (!currentSelected && accounts.length > 0) {
          this.selectedAccount.set(accounts[0]);
        }
        return;
      }
      
      // Check if the current account is still valid
      const isCurrentValid = currentSelected && currentSelected.balance >= amountValue;
      
      if (!isCurrentValid) {
        // Find a valid account that covers the amount`
        const validAccount = accounts.find(acc => acc.balance >= amountValue);
        this.selectedAccount.set(validAccount || null);
      }
    });
  }

  ngOnInit(): void {
    this.transactionState.isLoadingSendingTransaction.set(false);
    this.transactionState.errorSendingTransaction.set(null);
    
    this.loadCreditCardData();
  }

  get isFormValid(): boolean {
    const amountValue = this.amount();
    const selectedAccount = this.selectedAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid;
  }

  retryLoadCreditCardData() {
    this.loadCreditCardData();
  }

  retryLoadAccountsData(): void {
    this.accountService.refresh();
  }

  /**
   * Select a category
   * @param category The category to select
   * @returns void
   */
  selectCategory(category: CategoryResponseDTO): void {
    this.selectedCategory.set(category);
  }

  onClose() {
    this.close.emit();
  }

  onSubmit() {
    if (!this.isFormValid) return;

    const amountValue = this.amount();
    const descriptionValue = this.description();
    const creditCardResourceCode = this.creditCardResourceCode() || '';
    const accountResourceCode = this.selectedAccount()?.resourceCode || '';
    const categoryResourceCode = this.selectedCategory()?.resourceCode || undefined;
    const dateTimestamp = this.dateService.getUtcDatetime().getTime();

    const transactionRequest = TransactionRequestDTO.generateCreditCardPaymentTransaction(
      amountValue,
      descriptionValue,
      creditCardResourceCode,
      accountResourceCode,
      categoryResourceCode,
      dateTimestamp
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
          this.creditCardService.refresh();
        }
      }, error: (error) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        console.error('Error creating credit card payment:', error);
        if (error.status === 500 || error.status === 0) {
          this.transactionState.errorSendingTransaction.set('Error guardando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.errorSendingTransaction.set(error.error.message || 'Error guardando la transacción.');
        }
      }
    });
  }

  private loadCreditCardData(): void {
    this.creditCardState.isLoadingGetCreditCard.set(true);

    this.creditCardService.getByResourceCode(this.creditCardResourceCode()!).subscribe({
      next: (response: ApiResponseDTO<CreditCardResponseDTO>) => {
        if (response.success && response.data) {
          const creditCardData = response.data;
          this.creditCardData.set(creditCardData);
        }
        this.creditCardState.isLoadingGetCreditCard.set(false);
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.creditCardState.errorGetCreditCard.set('Error obteniendo los datos de la tarjeta de crédito. Por favor, inténtalo de nuevo.');
        } else {
          this.creditCardState.errorGetCreditCard.set(error.error.message || 'Error obteniendo los datos de la tarjeta de crédito.');
        }
      }
    });
  }
}
