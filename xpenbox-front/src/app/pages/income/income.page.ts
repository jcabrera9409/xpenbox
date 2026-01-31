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

@Component({
  selector: 'app-income-page',
  imports: [CommonModule, FormsModule, SummaryCard, IncomeAssignModal, LoadingUi, IncomeEditionModal, RetryComponent, CreateFirstComponent],
  templateUrl: './income.page.html',
  styleUrl: './income.page.css',
})
export class IncomePage {
  incomeState = incomeState;
  
  // Income assignment modal state
  incomeAssignModalVisible = signal<boolean>(false)
  selectedIncomeResourceCode = signal<string | null>(null);

  // Income edition modal state
  showIncomeEditionModal = signal(false);
  resourceCodeIncomeSelected = signal<string | null>(null);

  // Control of the filter accordion
  filterExpanded = signal<boolean>(false);
  
  // Filter for pending incomes
  showOnlyPending = signal<boolean>(false);
  
  // Temporary date inputs
  tempStartDate = signal<string>('');
  tempEndDate = signal<string>('');
  
  // Date range signals
  startDate = this.incomeState.startDate;
  endDate = this.incomeState.endDate;
  
  // Formatted dates for display from input values
  formattedStartDateDisplay = computed(() => {
    const dateStr = this.tempStartDate();
    if (!dateStr) return '';
    const date = this.dateService.parseDateIsoString(dateStr);
    return this.dateService.format(date.getTime(), 'short');
  });
  
  formattedEndDateDisplay = computed(() => {
    const dateStr = this.tempEndDate();
    if (!dateStr) return '';
    const date = this.dateService.parseDateIsoString(dateStr);
    return this.dateService.format(date.getTime(), 'short');
  });
  totalIncome = signal<number>(0);
  totalAllocated = signal<number>(0);
  totalPending = signal<number>(0);
  
  // Filtered incomes based on pending filter
  filteredIncomes = computed(() => {
    const incomes = this.incomeState.incomes();
    if (this.showOnlyPending()) {
      return incomes.filter(income => income.totalAmount - income.allocatedAmount > 0);
    }
    return incomes;
  });
  
  // Max date for date inputs (today)
  minDate!: string; 
  maxDate!: string;

  constructor(
    private incomeService: IncomeService,
    private router: Router,
    private dateService: DateService
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    const fiveYearsAgo = new Date(this.getTodayDate().setFullYear(this.getTodayDate().getFullYear() - 5));
    this.minDate = this.dateService.format(fiveYearsAgo.getTime(), 'ISO').split('T')[0];
    this.maxDate = this.dateService.format(this.getTodayDate().getTime(), 'ISO').split('T')[0];

    this.tempStartDate.set(this.dateService.format((this.startDate() || this.getFirstDayOfPreviousMonth()).getTime(), 'ISO').split('T')[0]);
    this.tempEndDate.set(this.dateService.format((this.endDate() || this.getTodayDate()).getTime(), 'ISO').split('T')[0]);

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
  
  openEditIncomeModal(resourceCode: string): void {
    // TODO: Implementar modal de edición
    console.log('Editar ingreso:', resourceCode);
  }
  
  deleteIncome(resourceCode: string): void {
    // TODO: Implementar eliminación
    console.log('Eliminar ingreso:', resourceCode);
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

}
