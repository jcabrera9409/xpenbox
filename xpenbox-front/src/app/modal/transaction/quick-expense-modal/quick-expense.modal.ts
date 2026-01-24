import { Component, effect, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';
import { accountState } from '../../../feature/account/service/account.state';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { CategoryService } from '../../../feature/category/service/category.service';
import { AccountService } from '../../../feature/account/service/account.service';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { AccountCreditDTO, AccountCreditType } from '../dto/account-credit.dto';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';

@Component({
  selector: 'app-quick-expense-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './quick-expense.modal.html',
  styleUrl: './quick-expense.modal.css',
})
export class QuickExpenseModal {

  close = output<void>();

  // Application states
  categoryState = categoryState;
  accountState = accountState;
  creditCardState = creditCardState;  

  selectedCategory = signal<CategoryResponseDTO | null>(null);
  selectedAccount = signal<AccountCreditDTO | null>(null);
  categoriesList = signal<CategoryResponseDTO[]>([]);
  accountCredits = signal<AccountCreditDTO[]>([]);

  // Numeric input state (signals)
  amount = signal('');
  showErrorAmount = signal(false);
  description = signal('');

  sendingForm = signal(false);
  errorMessage = signal<string | null>(null);

  // Numeric keyboard keys (1-9 and decimal point)
  keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.'];

  constructor(
    private categoryService: CategoryService,
    private accountService: AccountService,
    private creditCardService: CreditCardService,
    private transactionService: TransactionService
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

      if (!this.accountState.isLoading() && !this.creditCardState.isLoading()) {
        const accountCreditsList: AccountCreditDTO[] = [
          ...accounts.map(acc => ({
            resourceCode: acc.resourceCode,
            type: AccountCreditType.ACCOUNT,
            icon: 'account_balance',
            name: acc.name,
            balance: acc.balance,
            lastUsedDateTimestamp: acc.lastUsedDateTimestamp,
            usageCount: acc.usageCount,
          })),
          ...creditCards.map(cc => ({
            resourceCode: cc.resourceCode,
            type: AccountCreditType.CREDIT_CARD,
            icon: 'credit_card',
            name: cc.name,
            balance: cc.creditLimit - cc.currentBalance,
            lastUsedDateTimestamp: cc.lastUsedDateTimestamp,
            usageCount: cc.usageCount,
          }))
        ];

        const availableList = this.filterAndSortAccountCredits(accountCreditsList);
        this.accountCredits.set(availableList);

        if (availableList.length > 0 && !this.selectedAccount()) {
          this.selectedAccount.set(availableList[0] || null);
        }
      }
    });

    // Update selected account when amount changes
    effect(() => {
      const amountValue = parseFloat(this.amount());
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

  // Getters for filtered and sorted lists
  get categories(): CategoryResponseDTO[] {
    return this.categoryState.categories().filter(c => c.state);
  }

  // Getters for form validity
  get isFormValid(): boolean {
    const amountValue = parseFloat(this.amount());
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

  /**
   * Filter and sort account credits to have the two most recently used at the top,
   * followed by the two most used, and then the rest sorted by type and balance.
   * @param accountCreditsList The list of account credits to filter and sort.
   * @returns The filtered and sorted list of account credits.
   */
  private filterAndSortAccountCredits(accountCreditsList: AccountCreditDTO[]): AccountCreditDTO[] {
    const amountValue = parseFloat(this.amount());
    const filtered = accountCreditsList.filter(ac => ac.balance > 0 && ac.balance >= (isNaN(amountValue) ? 0 : amountValue));

    const sortedByLastUsed = [...filtered]
      .filter(ac => ac.lastUsedDateTimestamp)
      .sort((a, b) => (b.lastUsedDateTimestamp || 0) - (a.lastUsedDateTimestamp || 0));
    const lastTwo = sortedByLastUsed.slice(0, 2);
    const lastTwoIds = new Set(lastTwo.map(ac => ac.resourceCode));

    const restAfterLastTwo = filtered.filter(ac => !lastTwoIds.has(ac.resourceCode));
    const sortedByUsage = [...restAfterLastTwo]
      .sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0));
    const mostUsedTwo = sortedByUsage.slice(0, 2);
    const mostUsedTwoIds = new Set(mostUsedTwo.map(ac => ac.resourceCode));

    const rest = restAfterLastTwo.filter(ac => !mostUsedTwoIds.has(ac.resourceCode))
      .sort((a, b) => {
        if (a.type === b.type) {
          return b.balance - a.balance;
        }
        return a.type === AccountCreditType.ACCOUNT ? -1 : 1;
      });

    return [...lastTwo, ...mostUsedTwo, ...rest];
  }

  /**
   * Handle key press from numeric keyboard
   * @param key The key that was pressed
   * @returns void
   */
  onKeyPress(key: string): void {
    this.showErrorAmount.set(false);
    const value = this.amount();
    
    // Validate decimal point
    if (key === '.' && value.includes('.')) return;

    // Avoid multiple leading zeros
    if (value === '0' && key === '0') return;
    
    // Limit length
    if (value.length >= 9) return;

    // If decimal point is pressed without a value, add 0.
    if (key === '.' && !value) {
      this.amount.set('0.');
      return;
    }

    // Avoid leading zeros
    if (key === '0' && !value) {
      this.amount.set(key);
      return;
    }
    this.amount.set(value + key);
  }

  /**
   * Handle backspace key press
   * @returns void
   */
  onBackspace(): void {
    this.showErrorAmount.set(false);
    const value = this.amount();
    this.amount.set(value.slice(0, -1));
  }

  /**
   * Handle clear key press
   * @returns void
   */
  onClear(): void {
    this.showErrorAmount.set(false);
    this.amount.set('');
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
   * Select an account
   * @param account The account to select
   * @returns void
   */
  selectAccount(account: AccountCreditDTO): void {
    this.selectedAccount.set(account);
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
   * Check if an account is selected
   * @param accountResourceCode The resource code of the account to check
   * @returns boolean
   */
  isSelectedAccount(accountResourceCode: string): boolean {
    return this.selectedAccount()?.resourceCode === accountResourceCode;
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
    
    const amountValue = parseFloat(this.amount());
    const descriptionValue = this.description();
    const selectedAccount = this.selectedAccount();
    const categorySelected = this.selectedCategory();

    const transactionRequest = selectedAccount?.type === AccountCreditType.ACCOUNT
      ? TransactionRequestDTO.generateExpenseAccountTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode)
      : TransactionRequestDTO.generateExpenseCreditCardTransaction(amountValue, descriptionValue, selectedAccount?.resourceCode || '', categorySelected?.resourceCode);

    this.sendingForm.set(true);
    this.errorMessage.set(null);
    
    this.transactionService.create(transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.sendingForm.set(false);

        if (response.success && response.data) {
          this.close.emit();
        } else {
          console.error('Error creating expense:', response.message);
          this.errorMessage.set('Error creating expense: ' + response.message);
        }
      }, error: (error) => {
        console.error('Error creating expense:', error);
        this.errorMessage.set(error.message || 'Error creating expense');
        this.sendingForm.set(false);
      }
    })
  }
}
