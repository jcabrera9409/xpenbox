import { Component, signal, ChangeDetectionStrategy, inject, PLATFORM_ID, effect, OnInit, untracked } from '@angular/core';
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
import { TransactionFilterRequestDTO } from '../../feature/transaction/model/transaction-filter.request.dto';
import { TransactionResponseDTO } from '../../feature/transaction/model/transaction.response.dto';
import { ApiResponseDTO } from '../../feature/common/model/api.response.dto';
import { PageableResponseDTO } from '../../feature/common/model/pageable.response.dto';
import { CategoryService } from '../../feature/category/service/category.service';
import { ActivatedRoute } from '@angular/router';
import { DateService } from '../../shared/service/date.service';

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
  filterType = signal<TransactionType | undefined>(undefined);
  filterDescription = signal<string>('');
  filterStartDate = signal<string>('');
  filterEndDate = signal<string>('');
  filterCategory = signal<string | undefined>(undefined);
  source = signal<string | undefined>(undefined);
  code = signal<string | undefined>(undefined);

  maxDate = signal<string>(new Date().toISOString().split('T')[0]);
  
  // Control de acordeón de filtros
  filtersExpanded = signal<boolean>(false);
  
  // Control de paginación - Cargar más
  currentPage = signal<number>(0);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);
  accumulatedTransactions = signal<TransactionResponseDTO[]>([]);

  constructor(
    private transactionService: TransactionService,
    private categoryService: CategoryService,
    private route: ActivatedRoute,
    private dateService: DateService
  ) { 
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }
    if (this.categoryState.categories().length === 0) {
      this.categoryService.load();
    }

    this.maxDate.set(this.dateService.format(this.dateService.getLocalDatetime().getTime(), 'ISO').split('T')[0]);

    this.route.queryParamMap.subscribe(params => {
      this.source.set(params.get('source') || undefined);
      this.code.set(params.get('code') || undefined);
    });

    effect(() => {
      this.source();
      this.code();
      untracked(() => this.loadInitialTransactions());
    });

    effect(() => {
      if (this.transactionState.transactionCreatedResourceCode()) {
        this.loadInitialTransactions();
        this.transactionState.transactionCreatedResourceCode.set(null);
      }
    });

  }

  loadInitialTransactions(): void {
    this.currentPage.set(0);
    this.accumulatedTransactions.set([]);

    this.resetFilters();
    this.loadTransactions();
  }

  private loadTransactions(): void {
    const filter = this.buildFilterRequest();
    this.transactionState.isLoadingFilteredList.set(true);
    this.transactionState.errorFilteredList.set(null);

    this.transactionService.filterTransactions(filter).subscribe({
      next: (response: ApiResponseDTO<PageableResponseDTO<TransactionResponseDTO>>) => {
        this.transactionState.isLoadingFilteredList.set(false);
        if (response.data && response.data.content) {
          const currentContent = this.accumulatedTransactions();
          const newContent = response.data.content;
          const accumulated = [...currentContent, ...newContent];
          this.accumulatedTransactions.set(accumulated);
          this.totalElements.set(response.data.totalElements);
          this.totalPages.set(response.data.totalPages);
        }
      }, error: (error) => {
        this.transactionState.isLoadingFilteredList.set(false);
        if (error.status === 500 || error.status === 0) {
          this.transactionState.errorFilteredList.set('Error del servidor. Por favor, inténtalo de nuevo.');
        } else {
          this.transactionState.errorFilteredList.set(error.error.message || 'Error cargando las transacciones.');

        }
      }
    });
  }

  private buildFilterRequest(): TransactionFilterRequestDTO {
    const filter = TransactionFilterRequestDTO.createEmpty();
    const source = this.source();
    const code = this.code();
    filter.transactionDateTimestampFrom = this.dateService.parseDateIsoString(this.filterStartDate()).setHours(0,0,0,0);
    filter.transactionDateTimestampTo = this.dateService.parseDateIsoString(this.filterEndDate()).setHours(23,59,59,999);
    filter.description = this.filterDescription().trim() || undefined;
    filter.pageNumber = this.currentPage();
    filter.transactionType = this.filterType() && this.filterType() !== TransactionType.ALL ? this.filterType() : undefined;
    filter.categoryResourceCode = this.filterCategory() && this.filterCategory() !== 'ALL' ? this.filterCategory() : undefined;

    if (source && code) {
      switch (source) {
        case 'account':
          filter.accountResourceCode = code;
          break;
        case 'income':
          filter.incomeResourceCode = code;
          break;
        case 'creditcard':
          filter.creditCardResourceCode = code;
          break;
      }
    }

    return filter;
  }

  hasMoreTransactions(): boolean {
    return this.currentPage() < this.totalPages() - 1;
  }

  loadMore(): void {
    if (!this.hasMoreTransactions()) return;

    this.currentPage.set(this.currentPage() + 1);
    this.loadTransactions();
  }

  reloadTransactions(): void {
    this.loadInitialTransactions();
  }

  resetFilters(): void {
    const now = this.dateService.getLocalDatetime();
    const pastDate = this.dateService.getLocalDatetime();
    pastDate.setMonth(now.getMonth() - 1);

    this.filterEndDate.set(this.dateService.format(now.getTime(), 'ISO').split('T')[0]);
    this.filterStartDate.set(this.dateService.format(pastDate.getTime(), 'ISO').split('T')[0]);
    this.filterType.set(TransactionType.ALL);
    this.filterDescription.set('');
    this.filterCategory.set('ALL');
  }

  applyFilters(): void {
    this.currentPage.set(0);
    this.accumulatedTransactions.set([]);
    this.loadTransactions();
    this.toggleFilters();
  }

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
