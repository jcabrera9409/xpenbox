import { Component, input, OnInit, output, signal } from '@angular/core';
import { transactionState } from '../../../feature/transaction/service/transaction.state';
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../../shared/components/retry-component/retry.component';
import { TransactionResponseDTO } from '../../../feature/transaction/model/transaction.response.dto';
import { TransactionService } from '../../../feature/transaction/service/transaction.service';
import { DateService } from '../../../shared/service/date.service';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { userState } from '../../../feature/user/service/user.state';
import { Router } from '@angular/router';

@Component({
  selector: 'app-transaction-detail-modal',
  imports: [CommonModule, LoadingUi, RetryComponent],
  templateUrl: './transaction-detail.modal.html',
  styleUrl: './transaction-detail.modal.css',
})
export class TransactionDetailModal implements OnInit {

  userLogged = userState.userLogged;

  resourceCodeSelected = input<string>('');

  transactionState = transactionState;

  transactionData = signal<TransactionResponseDTO | null>(null);

  close = output<void>();

  constructor(
    private transactionService: TransactionService,
    private dateService: DateService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.transactionState.isLoadingGetTransaction.set(false);
    this.transactionState.errorGetTransaction.set(null);
    this.loadTransactionData();
  }

  formatDate(timestamp: number): string {
    const date = this.dateService.toDate(timestamp || 0);
    const dateStr = this.dateService.format(date.getTime(), 'datetime');
    return dateStr;
  }

  retryLoadTransactionData() {
    this.loadTransactionData();
  }

  loadTransactionData() {
    transactionState.isLoadingGetTransaction.set(true);

    this.transactionService.getByResourceCode(this.resourceCodeSelected()).subscribe({
      next: (response: ApiResponseDTO<TransactionResponseDTO>) => {
        transactionState.isLoadingGetTransaction.set(false);
        if (response.success && response.data) {
          this.transactionData.set(response.data);
        }
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          transactionState.errorGetTransaction.set('Error cargando la transacción. Por favor, inténtalo de nuevo.');
        } else {
          transactionState.errorGetTransaction.set(error.error.message || 'Error cargando la transacción. Por favor, inténtalo de nuevo.');
        }
      }
    });
  }

  onViewIncomeHistory(): void {
    this.close.emit();
    this.router.navigate(['/landing/transaction'], { 
      queryParams: { 
        source: 'income', 
        code: this.transactionData()?.income?.resourceCode
      }});
  }

  onViewAccountHistory(): void {
    this.close.emit();
    this.router.navigate(['/landing/transaction'], { 
      queryParams: { 
        source: 'account', 
        code: this.transactionData()?.account?.resourceCode
      }});
  }

  onViewDestinationAccountHistory(): void {
    this.close.emit();
    this.router.navigate(['/landing/transaction'], { 
      queryParams: { 
        source: 'account', 
        code: this.transactionData()?.destinationAccount?.resourceCode
      }});
  }

  onViewCreditCardHistory(): void {
    this.close.emit();
    this.router.navigate(['/landing/transaction'], { 
      queryParams: { 
        source: 'creditcard', 
        code: this.transactionData()?.creditCard?.resourceCode
      }});
  }

  onClose(): void {
    this.close.emit();
  }
}
