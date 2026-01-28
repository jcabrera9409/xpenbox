import { Component, signal, ChangeDetectionStrategy, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { transactionState } from '../../feature/transaction/service/transaction.state';
import { categoryState } from '../../feature/category/service/category.state';
import { TransactionType } from '../../feature/transaction/model/transaction.request.dto';
import { TransactionCard } from '../../shared/cards/transaction-card/transaction.card';
import { TransactionService } from '../../feature/transaction/service/transaction.service';
import { RetryComponent } from '../../shared/components/retry-component/retry.component';
import { LoadingUi } from '../../shared/ui/loading-ui/loading.ui';
import { CreateFirstComponent } from '../../shared/components/create-first-component/create-first.component';

@Component({
  selector: 'app-transaction-page',
  standalone: true,
  imports: [CommonModule, FormsModule, TransactionCard, RetryComponent, LoadingUi, CreateFirstComponent],
  templateUrl: './transaction.page.html',
  styleUrl: './transaction.page.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionPage {

  transactionState = transactionState;
  categoryState = categoryState;
  transactionType = TransactionType;

  // Filtros
  filterType = signal<TransactionType | 'ALL'>('ALL');
  filterDescription = signal<string>('');
  filterStartDate = signal<string>('');
  filterEndDate = signal<string>('');
  filterCategory = signal<string>('ALL');
  
  // Control de acordeón de filtros
  filtersExpanded = signal<boolean>(false);

  constructor(
    private transactionService: TransactionService
  ) { 
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.transactionService.loadFilteredTransactions();
  }

  reloadTransactions(): void {
    this.transactionService.loadFilteredTransactions();
  }

  // Resetear filtros
  resetFilters(): void {
    this.filterType.set('ALL');
    this.filterDescription.set('');
    this.filterStartDate.set('');
    this.filterEndDate.set('');
    this.filterCategory.set('ALL');
  }

  // Toggle filtros
  toggleFilters(): void {
    this.filtersExpanded.set(!this.filtersExpanded());
  }

  // Métodos de acción
  viewDetail(resourceCode: string): void {
    console.log('Ver detalle de transacción:', resourceCode);
    // Aquí iría la lógica para mostrar el detalle
  }

  editTransaction(resourceCode: string): void {
    console.log('Editar transacción:', resourceCode);
    // Aquí iría la lógica para editar
  }

  deleteTransaction(resourceCode: string): void {
    console.log('Eliminar transacción:', resourceCode);
    // Aquí iría la lógica para eliminar
  }
}
