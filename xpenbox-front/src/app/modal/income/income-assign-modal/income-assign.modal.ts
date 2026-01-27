import { Component, effect, input, OnInit, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { accountState } from '../../../feature/account/service/account.state';
import { AccountService } from '../../../feature/account/service/account.service';
import { TransactionRequestDTO } from '../../../feature/transaction/model/transaction.request.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { IncomeService } from '../../../feature/income/service/income.service';
import { IncomeResponseDTO } from '../../../feature/income/model/income.response.dto';
import { VirtualKeyboardUi } from '../../../shared/ui/virtual-keyboard-ui/virtual-keyboard.ui';
import { AccountsCarouselComponent } from '../../../shared/components/accounts-carousel-component/accounts-carousel.component';
import { AccountCreditService } from '../../../shared/service/account-credit.service';
import { AccountCreditDTO } from '../../../shared/dto/account-credit.dto';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';

@Component({
  selector: 'app-income-assign-modal',
  standalone: true,
  imports: [CommonModule, VirtualKeyboardUi, AccountsCarouselComponent, LoadingUi],
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

  selectedAccount = signal<AccountCreditDTO | null>(null);
  accountsList = signal<AccountCreditDTO[]>([]);

  // Numeric input state (signals)
  amount = signal(0);
  defaultAmount = signal(0);
  description = signal('');

  sendingForm = signal(false);

  constructor(
    private incomeService: IncomeService,
    private accountService: AccountService,
    private transactionService: TransactionService,
    private accountCreditService: AccountCreditService
  ) {
    if (this.accountState.accounts().length === 0) {
      this.accountService.load();
    }

    // Auto-select first account when loaded and sort accounts
    effect(() => {
      const accounts = this.accountCreditService.combineAccountAndCreditCardData(this.accountState.accounts(), []);
      const filteredAccounts = this.accountCreditService.filterAndSortAccountCredits(accounts, 0);
      if (filteredAccounts.length > 0) {
        this.accountsList.set(filteredAccounts);
        
        if (!this.selectedAccount()) {
          this.selectedAccount.set(filteredAccounts[0] || null);
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
    const amountValue = this.amount();
    const selectedAccount = this.selectedAccount();

    const isAmountValid = !isNaN(amountValue) && amountValue > 0;
    const isAccountValid = selectedAccount !== null;

    return isAmountValid && isAccountValid;
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
    
    const amountValue = this.amount();
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
          this.incomeService.refresh();
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
          this.defaultAmount.set(pendingAllocation);
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
