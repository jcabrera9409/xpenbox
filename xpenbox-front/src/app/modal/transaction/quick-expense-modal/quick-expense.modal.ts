import { Component, effect, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';
import { accountState } from '../../../feature/account/service/account.state';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { AccountService } from '../../../feature/account/service/account.service';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { AccountCreditDTO, AccountCreditType } from '../../../shared/dto/account-credit.dto';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { VirtualKeyboardUi } from '../../../shared/ui/virtual-keyboard-ui/virtual-keyboard.ui';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { CategoriesCarouselComponent } from '../../../shared/components/categories-carousel-component/categories-carousel.component';
import { CategoryService } from '../../../feature/category/service/category.service';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { DateService } from '../../../shared/service/date.service';

@Component({
  selector: 'app-quick-expense-modal',
  standalone: true,
  imports: [CommonModule, VirtualKeyboardUi, AccountsCarouselComponent, CategoriesCarouselComponent, ModalButtonsUi],
  templateUrl: './quick-expense.modal.html',
  styleUrl: './quick-expense.modal.css',
})
export class QuickExpenseModal implements OnInit {

  close = output<void>();

  // Application states
  categoryState = categoryState;
  accountState = accountState;
  creditCardState = creditCardState;  
  transactionState = transactionState;

  selectedCategory = signal<CategoryResponseDTO | null>(null);
  selectedAccount = signal<AccountCreditDTO | null>(null);
  accountCredits = signal<AccountCreditDTO[]>([]);

  // Numeric input state (signals)
  amount = signal(0);
  description = signal('');

  constructor(
    private categoryService: CategoryService,
    private accountService: AccountService,
    private creditCardService: CreditCardService,
    private transactionService: TransactionService,
    private accountCreditService: AccountCreditService,
    private dateService: DateService
  ) {
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }
    if (this.creditCardState.creditCards().length === 0) {
      this.creditCardService.load();
    }

    // Combine accounts and credit cards into accountCredits
    effect(() => {
      const accounts = this.accountState.accounts();
      const creditCards = this.creditCardState.creditCards();

      if (!this.accountState.isLoadingGetList() && !this.creditCardState.isLoadingGetList()) {
        const accountCreditsList: AccountCreditDTO[] = this.accountCreditService.combineAccountAndCreditCardData(accounts, creditCards);

        const availableList = this.accountCreditService.filterAndSortAccountCredits(accountCreditsList, this.amount());
        this.accountCredits.set(availableList);

        if (availableList.length > 0 && !this.selectedAccount()) {
          this.selectedAccount.set(availableList[0] || null);
        }
      }
    });

    // Update selected account when amount changes
    effect(() => {
      const amountValue = this.amount();
      const accounts = this.accountCredits();
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
  }

  // Getters for filtered and sorted lists
  get categories(): CategoryResponseDTO[] {
    return this.categoryState.categories().filter(c => c.state);
  }

  // Getters for form validity
  get isFormValid(): boolean {
    const amountValue = this.amount();
    const selectedAccount = this.selectedAccount();
    const categorySelected = this.selectedCategory();

    // Check if the selected account covers the amount`
    const isAccountValid = selectedAccount !== null &&
      !isNaN(amountValue) &&
      amountValue > 0 &&
      selectedAccount.balance >= amountValue;

    return isAccountValid && !!categorySelected;
  }

  retryAccountsAndCreditCards(): void {
    this.accountService.refresh();
    this.creditCardService.refresh();
  }

  /**
   * Select a category
   * @param category The category to select
   * @returns void
   */
  selectCategory(category: CategoryResponseDTO): void {
    this.selectedCategory.set(category);
  }
  /**
   * Close the modal
   * @returns void
   */
  onClose(): void {
    this.close.emit();
  }

  /**
   * Save the expense
   * @returns void
   */
  onSubmit(): void {
    
    if (!this.isFormValid) return;
    
    const amountValue = this.amount();
    const descriptionValue = this.description();
    const selectedAccount = this.selectedAccount();
    const categorySelected = this.selectedCategory();
    const dateTimestamp = this.dateService.getUtcDatetime().getTime();

    const transactionRequest = selectedAccount?.type === AccountCreditType.ACCOUNT
      ? TransactionRequestDTO.generateExpenseAccountTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode, dateTimestamp)
      : TransactionRequestDTO.generateExpenseCreditCardTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode, dateTimestamp);
    this.transactionState.isLoadingSendingTransaction.set(true);
    
    this.transactionService.create(transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        if (response.success && response.data) {
          this.close.emit();
          this.transactionState.successSendingTransaction.set(true);

          this.accountService.refresh();
          this.creditCardService.refresh();
          this.categoryService.refresh();
          this.transactionState.transactionCreatedResourceCode.set(response.data.resourceCode);
        } else {
          this.transactionState.errorSendingTransaction.set(response.message);
        }
      }, error: (error) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        console.error('Error creating expense:', error);
        if (error.status === 500 || error.status === 0) {
          this.transactionState.errorSendingTransaction.set('Error guardando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.errorSendingTransaction.set(error.error.message || 'Error guardando la transacción.');
        }
      }
    })
  }
}
