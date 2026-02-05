import { Component, computed, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { IncomeService } from '../../feature/income/service/income.service';
import { incomeState } from '../../feature/income/service/income.state';
import { FormsModule } from '@angular/forms';
import { SummaryCard } from '../../shared/cards/summary-card/summary.card';
import { IncomeAssignModal } from '../../modal/income/income-assign-modal/income-assign.modal';
import { LoadingUi } from '../../shared/ui/loading-ui/loading.ui';
import { IncomeEditionModal } from '../../modal/income/income-edition-modal/income-edition.modal';
import { RetryComponent } from '../../shared/components/retry-component/retry.component';
import { CreateFirstComponent } from '../../shared/components/create-first-component/create-first.component';
import { Router } from '@angular/router';
import { DateService } from '../../shared/service/date.service';
import { userState } from '../../feature/user/service/user.state';
import { ConfirmModal } from '../../modal/common/confirm-modal/confirm.modal';
import { ApiResponseDTO } from '../../feature/common/model/api.response.dto';
import { IncomeRequestDTO } from '../../feature/income/model/income.request.dto';
import { IncomeResponseDTO } from '../../feature/income/model/income.response.dto';
import { NotificationService } from '../../feature/common/service/notification.service';

@Component({
  selector: 'app-income-page',
  imports: [CommonModule, FormsModule, SummaryCard, IncomeAssignModal, LoadingUi, IncomeEditionModal, RetryComponent, CreateFirstComponent, ConfirmModal],
  templateUrl: './income.page.html',
  styleUrl: './income.page.css',
})
export class IncomePage {

  userLogged = userState.userLogged;

  incomeState = incomeState;
  
  // Income assignment modal state
  incomeAssignModalVisible = signal<boolean>(false)
  selectedIncomeResourceCode = signal<string | null>(null);

  // Income edition modal state
  showIncomeEditionModal = signal(false);
  showIncomeDeletionConfirmModal = signal(false);
  resourceCodeIncomeSelected = signal<string | null>(null);

  messageConfirmDeleteIncome = signal<string | null>(null);
  incomeDataSelected = signal<IncomeResponseDTO | null>(null);

  // Control of the filter accordion
  filterExpanded = signal<boolean>(false);
  
  // Filter for pending incomes
  showOnlyPending = signal<boolean>(false);
  
  // Temporary date inputs
  tempStartDate = signal<string>('');
  tempEndDate = signal<string>('');
  
  // Applied date filters (only updated on filter button click)
  appliedStartDate = signal<string>('');
  appliedEndDate = signal<string>('');
  
  // Date range signals
  startDate = this.incomeState.startDate;
  endDate = this.incomeState.endDate;
  
  // Formatted dates for display from applied values
  formattedStartDateDisplay = computed(() => {
    const dateStr = this.appliedStartDate();
    if (!dateStr) return '';
    const date = this.dateService.parseDateIsoString(dateStr);
    return this.dateService.format(date.getTime(), 'short');
  });
  
  formattedEndDateDisplay = computed(() => {
    const dateStr = this.appliedEndDate();
    if (!dateStr) return '';
    const date = this.dateService.parseDateIsoString(dateStr);
    return this.dateService.format(date.getTime(), 'short');
  });
  totalIncome = signal<number>(0);
  totalAllocated = signal<number>(0);
  totalPending = signal<number>(0);
  
  // Filtered and sorted incomes based on pending allocation and date range
  // Incomes with pending allocations appear first, sorted by most recent date
  filteredIncomes = computed(() => {
    const incomes = this.incomeState.incomes();
    const incomesSorted = incomes.slice().sort((a, b) => {
      const pendingA = a.totalAmount - a.allocatedAmount;
      const pendingB = b.totalAmount - b.allocatedAmount;

      if (pendingA > 0 && pendingB === 0) return -1;
      if (pendingA === 0 && pendingB > 0) return 1;

      return b.incomeDateTimestamp - a.incomeDateTimestamp;
    });
    if (this.showOnlyPending()) {
      return incomesSorted.filter(income => income.totalAmount - income.allocatedAmount > 0);
    }
    return incomesSorted;
  });
  
  // Max date for date inputs (today)
  minDate!: string; 
  maxDate!: string;

  constructor(
    private incomeService: IncomeService,
    private router: Router,
    private dateService: DateService,
    private notificationService: NotificationService  
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    const fiveYearsAgo = new Date(this.getTodayDate().setFullYear(this.getTodayDate().getFullYear() - 5));
    this.minDate = this.dateService.format(fiveYearsAgo.getTime(), 'ISO').split('T')[0];
    this.maxDate = this.dateService.format(this.getTodayDate().getTime(), 'ISO').split('T')[0];

    const startDateStr = this.dateService.format((this.startDate() || this.getFirstDayOfPreviousMonth()).getTime(), 'ISO').split('T')[0];
    const endDateStr = this.dateService.format((this.endDate() || this.getTodayDate()).getTime(), 'ISO').split('T')[0];
    
    this.tempStartDate.set(startDateStr);
    this.tempEndDate.set(endDateStr);
    this.appliedStartDate.set(startDateStr);
    this.appliedEndDate.set(endDateStr);

    if (!this.startDate() || !this.endDate()) {
      this.startDate.set(this.getFirstDayOfPreviousMonth());
      this.endDate.set(this.getTodayDate());
    }

    this.incomeService.load();

    effect(() => {
      const incomes = this.incomeState.incomes();
      const totalIncome = incomes.reduce((sum, income) => sum + income.totalAmount, 0);
      const totalAllocated = incomes.reduce((sum, income) => sum + income.allocatedAmount, 0);
      const totalPending = totalIncome - totalAllocated;

      this.totalIncome.set(totalIncome);
      this.totalAllocated.set(totalAllocated);
      this.totalPending.set(totalPending);
    });
  }

  applyFilter(): void {
    const parsedStart = this.dateService.parseDateIsoString(this.tempStartDate());
    const parsedEnd = this.dateService.parseDateIsoString(this.tempEndDate());

    this.startDate.set(parsedStart);
    this.endDate.set(parsedEnd);
    this.appliedStartDate.set(this.tempStartDate());
    this.appliedEndDate.set(this.tempEndDate());
    this.filterExpanded.set(false); 

    this.reloadIncomes();
  }
  
  toggleFilter(): void {
    this.filterExpanded.set(!this.filterExpanded());
  }
  
  openCreateIncomeModal(resourceCodeIncomeSelected: string | null = null): void {
    this.resourceCodeIncomeSelected.set(resourceCodeIncomeSelected);
    this.showIncomeEditionModal.set(true);
  }

  closeIncomeEditionModal() {
    this.showIncomeEditionModal.set(false);
  }
  
  openDeleteIncomeModal(resourceCode: string): void {
    this.resourceCodeIncomeSelected.set(resourceCode);
    this.loadIncomeData();
    this.showIncomeDeletionConfirmModal.set(true);
    console.log('Eliminar ingreso:', resourceCode);
  }

  confirmDeleteIncome(resourceCode: string) {
    this.incomeState.isLoadingSendingIncome.set(true);

    this.incomeService.delete(resourceCode).subscribe({
      next: () => {
        this.incomeState.isLoadingSendingIncome.set(false);
        this.showIncomeDeletionConfirmModal.set(false);
        this.reloadIncomes();

        this.notificationService.success('Ingreso eliminado correctamente.');
      }, 
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.incomeState.errorSendingIncome.set('Ocurrió un error al eliminar el ingreso. Por favor, intenta nuevamente.');
        } else {
          this.incomeState.errorSendingIncome.set(error.error.message || 'Ocurrió un error al eliminar el ingreso. Por favor, intenta nuevamente.');
        }
        this.incomeState.isLoadingSendingIncome.set(false);
      }
    });

  }

  closeDeleteIncomeModal() {
    this.showIncomeDeletionConfirmModal.set(false);
    this.resourceCodeIncomeSelected.set(null);
  }
  
  openIncomeAssignModal(resourceCode: string): void {
    this.selectedIncomeResourceCode.set(resourceCode);
    this.incomeAssignModalVisible.set(true);
  }

  closeIncomeAssignModal() {
    this.incomeAssignModalVisible.set(false);
    this.selectedIncomeResourceCode.set(null);
  }
  
  viewIncomeTransactions(resourceCode: string): void {
    this.router.navigate(['/landing/transaction'], { 
      queryParams: { 
        source: 'income', 
        code: resourceCode
      }});
  }
  
  reloadIncomes(): void {
    this.incomeService.refresh();
  }
  
  formatDate(timestamp: number): string {
    const date = this.dateService.toDate(timestamp);
    return this.dateService.format(date.getTime(), 'short');
  }
  
  formatDateRange(date: Date): string {
    return this.dateService.format(date.getTime(), 'short');
  }
  
  private getTodayDate(): Date {
    return this.dateService.getLocalDatetime();
  }
  
  private getFirstDayOfPreviousMonth(): Date {
    const today = this.getTodayDate();
    const firstDayPrevMonth = this.dateService.getLocalDatetime();
    firstDayPrevMonth.setMonth(today.getMonth() - 1);
    firstDayPrevMonth.setDate(1);
    firstDayPrevMonth.setHours(0, 0, 0, 0);
    return firstDayPrevMonth;
  }

  private loadIncomeData(): void {
    if (!this.resourceCodeIncomeSelected()) return;

    this.incomeState.isLoadingGetIncome.set(true);

    this.incomeService.getByResourceCode(this.resourceCodeIncomeSelected()!).subscribe({
      next: (data: ApiResponseDTO<IncomeResponseDTO>) => {
        this.incomeState.isLoadingGetIncome.set(false);
        this.incomeDataSelected.set(data.data);
        this.updateDeleteIncomeConfirmMessage(); 
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.incomeState.errorGetIncome.set('Ocurrió un error al cargar el ingreso. Por favor, intenta nuevamente.');
        } else {
          this.incomeState.errorGetIncome.set(error.error.message || 'Ocurrió un error al cargar el ingreso. Por favor, intenta nuevamente.');
        }
        this.incomeState.isLoadingGetIncome.set(false);
      }
    });
  }

  private updateDeleteIncomeConfirmMessage(): void {
    if (!this.incomeDataSelected()) return;

    const incomeConcept = this.incomeDataSelected()!.concept;
    const amount = this.incomeDataSelected()!.totalAmount;

    this.messageConfirmDeleteIncome.set(`¿Estás seguro de que deseas eliminar el ingreso "${incomeConcept}" por un monto de ${this.userLogged()?.currency} ${amount.toFixed(2)}? Esta acción no se puede deshacer.`);
  }
}
