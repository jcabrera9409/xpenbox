import { Component, effect, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';
import { accountState } from '../../../feature/account/service/account.state';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { CategoryService } from '../../../feature/category/service/category.service';
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
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';

@Component({
  selector: 'app-quick-expense-modal',
  standalone: true,
  imports: [CommonModule, VirtualKeyboardUi, AccountsCarouselComponent, LoadingUi],
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
  categoriesList = signal<CategoryResponseDTO[]>([]);
  accountCredits = signal<AccountCreditDTO[]>([]);

  // Numeric input state (signals)
  amount = signal(0);
  description = signal('');

  constructor(
    private categoryService: CategoryService,
    private accountService: AccountService,
    private creditCardService: CreditCardService,
    private transactionService: TransactionService,
    private accountCreditService: AccountCreditService
  ) {
    if (this.categoryState.categories().length === 0) {
      this.categoryService.load();
    }
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }
    if (this.creditCardState.creditCards().length === 0) {
      this.creditCardService.load();
    }

    // Auto-select first category when loaded
    effect(() => {
      const categories = this.categoryState.categories().filter(c => c.state);
      if (categories.length > 0 && !this.selectedCategory()) {
        const finalOrder = this.filterAndSortCategories([...categories]);
        this.categoriesList.set(finalOrder);
        this.selectedCategory.set(finalOrder[0] || null);
      }
    });

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

  /**
   * Filter and sort categories to have the two most recently used at the top,
   * followed by the rest sorted by usage count.
   * @param categories The list of categories to filter and sort.
   * @returns The filtered and sorted list of categories.
   */
  private filterAndSortCategories(categories: CategoryResponseDTO[]): CategoryResponseDTO[] {
    const sortedByLastUsed = [...categories]
      .filter(c => c.lastUsedDateTimestamp)
      .sort((a, b) => (b.lastUsedDateTimestamp || 0) - (a.lastUsedDateTimestamp || 0));

    const lastTwo = sortedByLastUsed.slice(0, 2);
    const lastTwoIds = new Set(lastTwo.map(c => c.resourceCode));

    const rest = categories
      .filter(c => !lastTwoIds.has(c.resourceCode))
      .sort((a, b) => b.usageCount - a.usageCount);

    return [...lastTwo, ...rest];
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
   * Check if a category is selected
   * @param categoryResourceCode The resource code of the category to check
   * @returns boolean
   */
  isSelectedCategory(categoryResourceCode: string): boolean {
    return this.selectedCategory()?.resourceCode === categoryResourceCode;
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

    const transactionRequest = selectedAccount?.type === AccountCreditType.ACCOUNT
      ? TransactionRequestDTO.generateExpenseAccountTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode)
      : TransactionRequestDTO.generateExpenseCreditCardTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode);

    this.transactionState.isLoadingSendingTransaction.set(true);
    
    this.transactionService.create(transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.transactionState.isLoadingSendingTransaction.set(false);
        if (response.success && response.data) {
          this.close.emit();
          this.transactionState.successSendingTransaction.set(true);

          this.accountService.refresh();
          this.creditCardService.refresh();
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
