import { Component, effect, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';
import { accountState } from '../../../feature/account/service/account.state';
import { creditCardState } from '../../../feature/creditcard/service/creditcard.state';
import { CategoryService } from '../../../feature/category/service/category.service';
import { AccountService } from '../../../feature/account/service/account.service';
import { CreditCardService } from '../../../feature/creditcard/service/creditcard.service';
import { CategoryRequestDTO } from '../../../feature/category/model/categoryRequestDTO';
import { CategoryResponseDTO } from '../../../feature/category/model/categoryResponseDTO';

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

  // Numeric input state (signals)
  amount = signal('');
  description = signal('');
  loading = signal(false);
  showError = signal(false);

  constructor(
    private categoryService: CategoryService,
    private accountService: AccountService,
    private creditCardService: CreditCardService
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
      const categories = this.categories;
      if (categories.length > 0 && !this.selectedCategory()) {
        this.selectedCategory.set(categories[0] || null);
      }
    });
  } 

  get categories(): CategoryResponseDTO[] {
    return this.categoryState.categories().filter(c => c.state);
  }

  accounts = [
    { id: 1, name: 'Efectivo', type: 'cash', balance: 1250.50 },
    { id: 2, name: 'Tarjeta Débito', type: 'debit', balance: 3840.00 },
    { id: 3, name: 'Tarjeta Crédito', type: 'credit', balance: 5000.00 },
    { id: 4, name: 'Cuenta Bancaria', type: 'bank', balance: 12500.75 },
  ];

  selectedAccount = signal(this.accounts[0]);

  // Numeric keyboard keys (1-9 and decimal point)
  keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.'];

  onKeyPress(key: string) {
    this.showError.set(false);
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

  onBackspace() {
    this.showError.set(false);
    const value = this.amount();
    this.amount.set(value.slice(0, -1));
  }

  onClear() {
    this.showError.set(false);
    this.amount.set('');
  }

  onCategoryChange(categoryResourceCode: string) {
    const category = this.categoryState.categories().find(c => c.resourceCode === categoryResourceCode);
    if (category) {
      this.selectedCategory.set(category);
    }
  }

  onAccountChange(accountId: string) {
    const account = this.accounts.find(a => a.id === parseInt(accountId));
    if (account) {
      this.selectedAccount.set(account);
    }
  }

  selectCategory(category: CategoryResponseDTO) {
    this.selectedCategory.set(category);
  }

  selectAccount(account: { id: number; name: string; type: string; balance: number }) {
    this.selectedAccount.set(account);
  }

  isSelectedCategory(categoryResourceCode: string): boolean {
    return this.selectedCategory()?.resourceCode === categoryResourceCode;
  }

  isSelectedAccount(accountId: number): boolean {
    return this.selectedAccount().id === accountId;
  }

  onClose() {
    this.close.emit();
  }

  onSave() {
    // Validate that there is an amount
    const amountValue = parseFloat(this.amount());
    if (!this.amount() || isNaN(amountValue) || amountValue <= 0) {
      this.showError.set(true);
      return;
    }

    this.loading.set(true);
    
    // Simulate saving (placeholder)
    setTimeout(() => {
      this.loading.set(false);
      
      // Here would be the logic to save the expense
      console.log({
        amount: amountValue,
        description: this.description(),
        category: this.selectedCategory(),
        account: this.selectedAccount(),
      });
      
      // Reset and close modal
      this.onClose();
    }, 1200);
  }
}
