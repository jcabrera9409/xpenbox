import { Component, effect, input, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { accountState } from '../../../feature/account/service/account.state';
import { AccountService } from '../../../feature/account/service/account.service';
import { AccountResponseDTO } from '../../../feature/account/model/account.response.dto';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { IncomeService } from '../../../feature/income/service/income.service';
import { IncomeResponseDTO } from '../../../feature/income/model/income.response.dto';

@Component({
  selector: 'app-income-assign-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './income-assign.modal.html',
  styleUrl: './income-assign.modal.css',
})
export class IncomeAssignModal implements OnInit {

  incomeResourceCode = input<string | null>();
  close = output<void>();

  // Application states
  accountState = accountState;
  transactionState = transactionState;

  loading = signal<boolean>(false);
  errorLoading = signal<string | null>(null);

  selectedAccount = signal<AccountResponseDTO | null>(null);
  accountsList = signal<AccountResponseDTO[]>([]);

  // Numeric input state (signals)
  amount = signal('');
  showErrorAmount = signal(false);
  description = signal('');

  sendingForm = signal(false);

  // Numeric keyboard keys (1-9 and decimal point)
  keys = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '.'];

  constructor(
    private incomeService: IncomeService,
    private accountService: AccountService,
    private transactionService: TransactionService
  ) {
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }

    // Auto-select first account when loaded and sort accounts
    effect(() => {
      const accounts = this.accountState.accounts().filter(a => a.state);
      if (accounts.length > 0) {
        const sortedAccounts = this.filterAndSortAccounts([...accounts]);
        this.accountsList.set(sortedAccounts);
        
        if (!this.selectedAccount()) {
          this.selectedAccount.set(sortedAccounts[0] || null);
        }
      }
    });
  }

  ngOnInit(): void { 
    this.transactionState.error.set(null);
    this.loadIncomeData();
  }

  // Getters for form validity
  get isFormValid(): boolean {
    const amountValue = parseFloat(this.amount());
    const selectedAccount = this.selectedAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid;
  }

  /**
   * Filter and sort accounts to have the two most recently used at the top,
   * followed by the rest sorted by usage count and balance.
   * @param accounts The list of accounts to filter and sort.
   * @returns The filtered and sorted list of accounts.
   */
  private filterAndSortAccounts(accounts: AccountResponseDTO[]): AccountResponseDTO[] {
    const sortedByLastUsed = [...accounts]
      .filter(a => a.lastUsedDateTimestamp)
      .sort((a, b) => (b.lastUsedDateTimestamp || 0) - (a.lastUsedDateTimestamp || 0));

    const lastTwo = sortedByLastUsed.slice(0, 2);
    const lastTwoIds = new Set(lastTwo.map(a => a.resourceCode));

    const rest = accounts
      .filter(a => !lastTwoIds.has(a.resourceCode))
      .sort((a, b) => {
        const usageDiff = b.usageCount - a.usageCount;
        if (usageDiff !== 0) return usageDiff;
        return b.balance - a.balance;
      });

    return [...lastTwo, ...rest];
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
   * Select an account
   * @param account The account to select
   * @returns void
   */
  selectAccount(account: AccountResponseDTO): void {
    this.selectedAccount.set(account);
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
   * Save the income assignment
   * @returns void
   */
  onSubmit(): void {
    
    if (!this.isFormValid) return;
    
    const amountValue = parseFloat(this.amount());
    const descriptionValue = this.description();
    const incomeResourceCode = this.incomeResourceCode();
    const selectedAccount = this.selectedAccount();

    const transactionRequest = TransactionRequestDTO.generateIncomeAssignmentTransaction(
      amountValue,
      descriptionValue,
      incomeResourceCode || '',
      selectedAccount?.resourceCode || ''
    );

    this.sendingForm.set(true);
    this.transactionState.error.set(null);
    
    this.transactionService.create(transactionRequest).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        this.sendingForm.set(false);

        if (response.success && response.data) {
          this.close.emit();
          this.transactionState.isSuccess.set(true);

          this.accountService.refresh();
        } else {
          this.transactionState.error.set(response.message);
        }
      }, error: (error) => {
        this.sendingForm.set(false);
        console.error('Error creating income assignment:', error);
        if (error.status === 500 || error.status === 0) {
          this.transactionState.error.set('Error guardando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.error.set(error.error.message || 'Error guardando la transacción.');
        }
      }
    })
  }

  private loadIncomeData(): void {
    this.loading.set(true);

    console.log('Loading income data for resource code:', this.incomeResourceCode());

    this.incomeService.getByResourceCode(this.incomeResourceCode()!).subscribe({
      next: (response: ApiResponseDTO<IncomeResponseDTO>) => {
        if (response.success && response.data) {
          const incomeData = response.data;
          const pendingAllocation = incomeData.totalAmount - incomeData.allocatedAmount;
          this.amount.set(pendingAllocation.toFixed(2));
        } else {
          console.error('Error fetching income data:', response.message);
          this.errorLoading.set('Error fetching income data: ' + response.message);
        }
        this.loading.set(false);
      },
      error: (error) => {
        console.error('Error fetching income data:', error);
        this.errorLoading.set(error.message || 'Error fetching income data');
        this.loading.set(false);
      }
    });
  }
}
