import { Component, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-quick-expense-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './quick-expense.modal.html',
  styleUrl: './quick-expense.modal.css',
})
export class QuickExpenseModal {

  close = output<void>();

  // Numeric input state (signals)
  amount = signal('');
  description = signal('');
  loading = signal(false);
  showError = signal(false);

  // Example categories and accounts (replace with real data integration)
  categories = [
    { id: 1, name: 'Alimentos', color: '#F87171'},
    { id: 2, name: 'Transporte', color: '#60A5FA'},
    { id: 3, name: 'Entretenimiento', color: '#FBBF24'},
    { id: 4, name: 'Salud', color: '#34D399'},
    { id: 5, name: 'Educación', color: '#A78BFA'},
    { id: 6, name: 'Otros', color: '#9CA3AF'},
  ];

  accounts = [
    { id: 1, name: 'Efectivo', type: 'cash', balance: 1250.50 },
    { id: 2, name: 'Tarjeta Débito', type: 'debit', balance: 3840.00 },
    { id: 3, name: 'Tarjeta Crédito', type: 'credit', balance: 5000.00 },
    { id: 4, name: 'Cuenta Bancaria', type: 'bank', balance: 12500.75 },
  ];

  selectedCategory = signal(this.categories[0]);
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

  onCategoryChange(categoryId: string) {
    const category = this.categories.find(c => c.id === parseInt(categoryId));
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

  selectCategory(category: { id: number; name: string; color: string }) {
    this.selectedCategory.set(category);
  }

  selectAccount(account: { id: number; name: string; type: string; balance: number }) {
    this.selectedAccount.set(account);
  }

  isSelectedCategory(categoryId: number): boolean {
    return this.selectedCategory().id === categoryId;
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
