import { Component, effect, input, OnInit, output, signal } from '@angular/core';
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
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { DateService } from '../../../shared/service/date.service';
import { CategoriesCarouselComponent } from '../../../shared/components/categories-carousel-component/categories-carousel.component';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { userState } from '../../../feature/user/service/user.state';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';
import { InputComponent } from '../../../shared/components/input-component/input.component';
import { InputAmountComponent } from '../../../shared/components/input-amount-component/input-amount-component';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { GenericModal } from '../../common/generic-modal/generic.modal';

@Component({
  selector: 'app-creditcard-payment-modal',
  imports: [CommonModule, LoadingUi, RetryComponent, AccountsCarouselComponent, CategoriesCarouselComponent, IconComponent, InputComponent, InputAmountComponent, ReactiveFormsModule, GenericModal],
  templateUrl: './creditcard-payment.modal.html',
  styleUrl: './creditcard-payment.modal.css',
})
export class CreditcardPaymentModal implements OnInit {

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

  formPayment!: FormGroup;
  maxDate = signal('');
  amountOutput = signal<number>(0);

  constructor(
    private fb: FormBuilder,
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

      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, this.amountOutput());
      this.accountsList.set(filteredAccounts);

      if (filteredAccounts.length > 0 && !this.selectedAccount()) {
          this.selectedAccount.set(filteredAccounts[0] || null);
      }
    });

    // Update selected account when amount changes
    effect(() => {
      const amountValue = this.amountOutput();
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

    this.initForms();
  }

  get isFormValid(): boolean {
    const amountValue = this.amountControl.value;
    const selectedAccount = this.selectedAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid && this.formPayment.valid;
  }

  get isOnlyOneCreditCard(): boolean {
    if (this.creditCardResourceCode()) {
      return true;
    } else {
      return this.onlyOneCreditCard();
    }
  }

  get amountControl(): FormControl {
    return this.formPayment.get('amount') as FormControl;
  }

  get descriptionControl(): FormControl {
    return this.formPayment.get('description') as FormControl;
  }

  get transactionDateControl(): FormControl {
    return this.formPayment.get('transactionDate') as FormControl;
  }

  initForms(): void {
    const today = this.dateService.toTimestamp(this.dateService.getLocalDatetime());
    const formattedDate = this.dateService.format(today, 'ISO-LOCAL');
    this.maxDate.set(formattedDate);

    this.formPayment = this.fb.group({
      amount: [null, [
        Validators.required,
        Validators.min(0.01)
      ]],
      description: [''],
      transactionDate: [formattedDate, Validators.required]
    });
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

    const amountValue = this.amountOutput();
    const descriptionValue = this.descriptionControl.value;
    const creditCardResourceCode = this.creditCardData()?.resourceCode || '';
    const accountResourceCode = this.selectedAccount()?.resourceCode || '';
    const categoryResourceCode = this.selectedCategory()?.resourceCode || undefined;
    const transactionDate = this.dateService.parseDatetimeIsoString(this.transactionDateControl.value);
    const transactionDateTimestamp = this.dateService.toTimestamp(transactionDate);

    const transactionRequest = TransactionRequestDTO.generateCreditCardPaymentTransaction(
      amountValue,
      descriptionValue,
      creditCardResourceCode,
      accountResourceCode,
      categoryResourceCode,
      transactionDateTimestamp
    );

    this.transactionService.submitTransaction(transactionRequest, () => this.successTransactionCreated());
  }

  private successTransactionCreated(): void {
    this.close.emit();
    this.accountService.refresh();
    this.creditCardService.refresh();
    this.transactionService.refresh();
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
