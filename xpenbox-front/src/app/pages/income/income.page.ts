import { Component, computed, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { IncomeService } from '../../feature/income/service/income.service';
import { incomeState } from '../../feature/income/service/income.state';
import { FormsModule } from '@angular/forms';
import { SummaryCard } from '../../shared/cards/summary-card/summary.card';

@Component({
  selector: 'app-income-page',
  imports: [CommonModule, FormsModule, SummaryCard],
  templateUrl: './income.page.html',
  styleUrl: './income.page.css',
})
export class IncomePage {
  incomeState = incomeState;
  
  // Control del acordeón de filtros (solo mobile)
  filterExpanded = signal<boolean>(false);
  
  // Filtro temporal (antes de aplicar)
  tempStartDate: string = this.getFirstDayOfCurrentMonth();
  tempEndDate: string = this.getTodayDate();
  
  // Filtro aplicado
  startDate = signal<string>(this.getFirstDayOfCurrentMonth());
  endDate = signal<string>(this.getTodayDate());
  
  // Fecha máxima permitida (hoy)
  maxDate = this.getTodayDate();
  
  // Ingresos filtrados por rango de fechas
  filteredIncomes = computed(() => {
    const start = this.startDate();
    const end = this.endDate();
    
    if (!start || !end) return this.incomeState.incomes();
    
    const startTimestamp = new Date(start).getTime();
    const endTimestamp = new Date(end + 'T23:59:59').getTime(); // Incluir todo el día final
    
    return this.incomeState.incomes().filter(income => {
      return income.incomeDateTimestamp >= startTimestamp && 
             income.incomeDateTimestamp <= endTimestamp;
    });
  });
  
  // Total de ingresos del mes seleccionado
  totalMonthIncome = computed(() => {
    return this.filteredIncomes().reduce((sum, income) => sum + income.totalAmount, 0);
  });
  
  // Total asignado del mes seleccionado
  totalMonthAllocated = computed(() => {
    return this.filteredIncomes().reduce((sum, income) => sum + income.allocatedAmount, 0);
  });
  
  // Total pendiente de asignar
  totalPendingAllocation = computed(() => {
    return this.totalMonthIncome() - this.totalMonthAllocated();
  });

  constructor(private incomeService: IncomeService) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.incomeService.load();
  }
  
  onStartDateChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.tempStartDate = target.value;
  }
  
  onEndDateChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.tempEndDate = target.value;
  }
  
  applyFilter(): void {
    this.startDate.set(this.tempStartDate);
    this.endDate.set(this.tempEndDate);
    this.filterExpanded.set(false); // Cerrar acordeón después de filtrar
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
  
  assignIncome(resourceCode: string): void {
    // TODO: Implementar modal de asignación de ingresos a cuentas
    console.log('Asignar ingreso a cuentas:', resourceCode);
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
  
  private getTodayDate(): string {
    const today = new Date();
    return this.formatDateToInput(today);
  }
  
  private getFirstDayOfCurrentMonth(): string {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    return this.formatDateToInput(firstDay);
  }
  
  private formatDateToInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
  
  private getCurrentMonth(): string {
    const now = new Date();
    return this.formatDateToMonth(now);
  }
  
  private formatDateToMonth(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  }
}
