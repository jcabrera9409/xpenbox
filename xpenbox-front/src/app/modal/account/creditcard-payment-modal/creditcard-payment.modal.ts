import { Component, effect, input, output, signal } from '@angular/core';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { AccountCreditDTO, AccountCreditType } from '../../../shared/dto/account-credit.dto';
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
import { userState } from '../../../feature/user/service/user.state';

@Component({
  selector: 'app-creditcard-payment-modal',
  imports: [CommonModule, LoadingUi, RetryComponent, VirtualKeyboardUi, ModalButtonsUi, AccountsCarouselComponent, CategoriesCarouselComponent],
  templateUrl: './creditcard-payment.modal.html',
  styleUrl: './creditcard-payment.modal.css',
})
export class CreditcardPaymentModal {

  userLogged = userState.userLogged;

  creditCardResourceCode = input<string | null>();
  close = output<void>();

  accountState = accountState;
  creditCardState = creditCardState;
  transactionState = transactionState;

  accountsList = signal<AccountCreditDTO[]>([]);
  selectedAccount = signal<AccountCreditDTO | null>(null);
  selectedCategory = signal<CategoryResponseDTO | null>(null);
  assignToCategory = signal<boolean>(false);

  creditCardList = signal<AccountCreditDTO[]>([]);
  creditCardData = signal<AccountCreditDTO | null>(null);
  onlyOneCreditCard = signal<boolean>(false);

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

    if (this.creditCardState.creditCards().length === 0) {
      this.creditCardService.load();
    }

    // Auto-select first credit-card when loaded and with balance > 0
    effect(() => {
      if (this.creditCardResourceCode()) {
        return;
      }

      // Generate credit card list updating its balances
      const list: CreditCardResponseDTO[] = this.creditCardState.creditCards().map(cc => {
        return {
          resourceCode: cc.resourceCode,
          name: cc.name,
          creditLimit: cc.currentBalance,
          currentBalance: 0,
          lastUsedDateTimestamp: cc.lastUsedDateTimestamp,
          usageCount: cc.usageCount,
          state: cc.state,
          billingDay: cc.billingDay,
          paymentDay: cc.paymentDay,
          closingDateTimestamp: cc.closingDateTimestamp
        };
      });
      const crediCards = this.accountCreditService.combineAccountAndCreditCardData([], list);
      const filteredCreditCards = crediCards.filter(cc => cc.balance > 0);

      if (filteredCreditCards.length == 1) {
        this.onlyOneCreditCard.set(true);
        this.creditCardData.set(filteredCreditCards[0]);
      } else if (filteredCreditCards.length > 0) {
        this.creditCardList.set(filteredCreditCards);
        this.onlyOneCreditCard.set(false);

        if (!this.creditCardData()) {
          this.creditCardData.set(filteredCreditCards[0]);
        }
      }
    });

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
    
    if (this.creditCardResourceCode()) {
      this.loadCreditCardData();
    }
  }

  get isFormValid(): boolean {
    const amountValue = this.amount();
    const selectedAccount = this.selectedAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid;
  }

  get isOnlyOneCreditCard(): boolean {
    if (this.creditCardResourceCode()) {
      return true;
    } else {
      return this.onlyOneCreditCard();
    }
  }

  retryLoadCreditCardData() {
    this.loadCreditCardData();
  }

  retryLoadCreditCardsData(): void {
    this.creditCardService.refresh();
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
    const creditCardResourceCode = this.creditCardData()?.resourceCode || '';
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

          if (!this.creditCardResourceCode()) {
            this.transactionState.transactionCreatedResourceCode.set(response.data.resourceCode);
          }
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
          const creditCardDTO: AccountCreditDTO = {
            resourceCode: creditCardData.resourceCode,
            type: AccountCreditType.CREDIT_CARD,
            icon: 'credit_card',
            name: creditCardData.name,
            balance: creditCardData.currentBalance,
            lastUsedDateTimestamp: creditCardData.lastUsedDateTimestamp,
            usageCount: creditCardData.usageCount
          };
          
          this.creditCardData.set(creditCardDTO);
        }
        this.creditCardState.isLoadingGetCreditCard.set(false);
        this.creditCardState.errorGetCreditCard.set(null);
      },
      error: (error) => {
        this.creditCardState.isLoadingGetCreditCard.set(false);
        if (error.status === 500 || error.status === 0) {
          this.creditCardState.errorGetCreditCard.set('Error obteniendo los datos de la tarjeta de crédito. Por favor, inténtalo de nuevo.');
        } else {
          this.creditCardState.errorGetCreditCard.set(error.error.message || 'Error obteniendo los datos de la tarjeta de crédito.');
        }
      }
    });
  }
}
