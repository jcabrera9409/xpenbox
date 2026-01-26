import { Component, computed, effect, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { IncomeService } from '../../feature/income/service/income.service';
import { incomeState } from '../../feature/income/service/income.state';
import { FormsModule } from '@angular/forms';
import { SummaryCard } from '../../shared/cards/summary-card/summary.card';
import { IncomeAssignModal } from '../../modal/income/income-assign-modal/income-assign.modal';

@Component({
  selector: 'app-income-page',
  imports: [CommonModule, FormsModule, SummaryCard, IncomeAssignModal],
  templateUrl: './income.page.html',
  styleUrl: './income.page.css',
})
export class IncomePage {
  incomeState = incomeState;
  
  // Income assignment modal state
  incomeAssignModalVisible = signal<boolean>(false)
  selectedIncomeResourceCode = signal<string | null>(null);

  // Control of the filter accordion
  filterExpanded = signal<boolean>(false);
  
  // Temporary date inputs
  tempStartDate!: string;
  tempEndDate!: string;
  
  // Date range signals
  startDate = this.incomeState.startDate;
  endDate = this.incomeState.endDate;
  totalIncome = signal<number>(0);
  totalAllocated = signal<number>(0);
  totalPending = signal<number>(0);
  
  // Max date for date inputs (today)
  minDate = this.formatDateToInput(new Date(this.getTodayDate().setFullYear(this.getTodayDate().getFullYear() - 5)));
  maxDate = this.formatDateToInput(this.getTodayDate());

  constructor(private incomeService: IncomeService) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.tempStartDate = this.formatDateToInput(this.startDate() || this.getFirstDayOfCurrentMonth());
    this.tempEndDate = this.formatDateToInput(this.endDate() || this.getTodayDate());

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
    const parsedStart = this.parseDateString(this.tempStartDate);
    const parsedEnd = this.parseDateString(this.tempEndDate);

    this.startDate.set(parsedStart);
    this.endDate.set(parsedEnd);
    this.filterExpanded.set(false); 

    this.reloadIncomes();
  }
  
  toggleFilter(): void {
    this.filterExpanded.set(!this.filterExpanded());
  }
  
  openCreateIncomeModal(): void {
    // TODO: Implementar modal de creación
    console.log('Abrir modal de creación de ingreso');
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
    // TODO: Implementar modal/página para ver transacciones de asignación
    console.log('Ver transacciones de asignación del ingreso:', resourceCode);
  }
  
  reloadIncomes(): void {
    this.incomeService.refresh();
  }
  
  formatDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('es-PE', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }
  
  private getTodayDate(): Date {
    return new Date();
  }
  
  private getFirstDayOfCurrentMonth(): Date {
    const today = new Date();
    return new Date(today.getFullYear(), today.getMonth(), 1);
  }
  
  private formatDateToInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private parseDateString(dateString: string): Date {
    // Parsear fecha en formato YYYY-MM-DD sin problemas de zona horaria
    const [year, month, day] = dateString.split('-').map(num => parseInt(num, 10));
    return new Date(year, month - 1, day);
  }

}
