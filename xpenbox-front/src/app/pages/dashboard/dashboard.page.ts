import { Component, OnInit, AfterViewInit, signal, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Chart, ChartConfiguration, registerables } from 'chart.js';
import { DashboardState } from '../../feature/dashboard/service/dashboard.state';
import { DashboardService } from '../../feature/dashboard/service/dashboard.service';
import { DashboardResponseModelDTO } from '../../feature/dashboard/model/dashboard.response.model.dto';
import { PeriodFilterRequestDTO } from '../../feature/dashboard/model/period-filter.request.dto';
import { ApiResponseDTO } from '../../feature/common/model/api.response.dto';
import { CategoryResponseDTO } from '../../feature/category/model/category.response.dto';
import { CreditCardResponseDTO } from '../../feature/creditcard/model/creditcard.response.dto';
import { TransactionResponseDTO } from '../../feature/transaction/model/transaction.response.dto';
import { DateService } from '../../shared/service/date.service';
import { TransactionType } from '../../feature/transaction/model/transaction.request.dto';
import { RouterLink } from '@angular/router';
import { LoadingUi } from '../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../shared/components/retry-component/retry.component';
import { TooltipUi } from '../../shared/ui/tooltip-ui/tooltip.ui';

@Component({
  selector: 'app-dashboard-page',
  imports: [CommonModule, RouterLink, LoadingUi, RetryComponent, TooltipUi],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.css',
})
export class DashboardPage implements OnInit, AfterViewInit {

  periodFilter = PeriodFilterRequestDTO;
  transactionType = TransactionType;
  dashboardState = DashboardState;
  dashboardData = signal<DashboardResponseModelDTO | null>(null);

  private platformId = inject(PLATFORM_ID);
  private expenseChart: Chart | null = null;
  private chartRegistered = false;

  showBalanceTooltip = signal<boolean>(false);

  // Datos del dashboard
  selectedPeriod = signal<PeriodFilterRequestDTO>(PeriodFilterRequestDTO.CURRENT_MONTH);
  currentBalance = signal<number>(0);
  openingBalance = signal<number>(0);
  percentageChangeBalance = signal<number>(0);
  percentageChangeBalanceAbs = signal<number>(0);
  incomeTotal = signal<number>(0);
  expenseTotal = signal<number>(0);
  netCashFlow = signal<number>(0);
  netCashFlowAbs = signal<number>(0);
  totalCategoryAmount = signal<number>(0);
  totalCreditLimit = signal<number>(0);
  totalCreditUsed = signal<number>(0);
  percentajeCreditUsed = signal<number>(0);

  categories = signal<CategoryResponseDTO[]>([]);
  creditCards = signal<CreditCardResponseDTO[]>([]);
  transactions = signal<TransactionResponseDTO[]>([]);

  constructor(
    private dashboardService: DashboardService,
    private dateService: DateService
  ) {
    // Registrar Chart.js en el constructor si estamos en el navegador
    if (isPlatformBrowser(this.platformId)) {
      Chart.register(...registerables);
      this.chartRegistered = true;
    }
  }

  get isCurrentMonthSelected(): boolean {
    return this.isPeriodSelected(PeriodFilterRequestDTO.CURRENT_MONTH);
  }

  getPercentageCategoryAmount(amount: number): number {
    const total = this.totalCategoryAmount();
    return total > 0 ? ((amount / total) * 100) : 0;
  }

  getPercentajeCreditUsed(creditCard: CreditCardResponseDTO): number {
    const totalLimit = creditCard.creditLimit;
    const used = creditCard.currentBalance;
    return totalLimit > 0 ? ((used / totalLimit) * 100) : 0;
  }

  isPeriodSelected(period: PeriodFilterRequestDTO): boolean {
    return this.selectedPeriod() === period;
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadDashboardData();
    }
  }

  ngAfterViewInit(): void {
    // Si hay categorías pendientes de renderizar, actualizar el gráfico
    if (isPlatformBrowser(this.platformId) && this.categories().length > 0) {
      // Usar setTimeout para asegurar que el canvas esté completamente renderizado
      setTimeout(() => {
        this.updateExpenseChart();
      }, 0);
    }
  }

  ngOnDestroy(): void {
    if (this.expenseChart) {
      this.expenseChart.destroy();
    }
  }

  changePeriod(period: PeriodFilterRequestDTO): void {
    this.selectedPeriod.set(period);
    this.loadDashboardData();
  }

  retryLoadDashboardData(): void {
    this.loadDashboardData();
  }

  getTransactionTypeLabel(type: TransactionType | undefined): string {
    return TransactionType.getLabel(type);
  }

  getTransactionBgColorClass(type: TransactionType | undefined): string {
    return TransactionType.getTransactionBgColorClass(type);
  }

  formatDate(timestamp: number): string {
    const today = this.dateService.getUtcDatetime().getTime();
    const dateToday = new Date(today);
    const dateTransaction = this.dateService.toDate(timestamp || 0);

    if (dateTransaction.toDateString() === dateToday.toDateString()) {
      return 'Hoy';
    } else if (dateTransaction.toDateString() === this.dateService.addDays(dateToday, -1).toDateString()) {
      return 'Ayer';
    } else if (dateTransaction.getFullYear() !== dateToday.getFullYear()) {
      return this.dateService.format(timestamp, 'short');
    }

    const dateStr = this.dateService.format(dateTransaction.getTime(), 'day-month');

    return dateStr
  }

  private updateDashboardData(): void {
    const data = this.dashboardData();
    if (!data) return;

    const currentBalance = data.current.currentBalance;
    const openingBalance = data.current.openingBalance;
    const percentageChange = openingBalance === 0 ? 100 : ((currentBalance - openingBalance) / Math.abs(openingBalance)) * 100;

    this.currentBalance.set(data.current.currentBalance);
    this.openingBalance.set(data.current.openingBalance);
    this.percentageChangeBalance.set(percentageChange);
    this.percentageChangeBalanceAbs.set(Math.abs(percentageChange));
    this.incomeTotal.set(data.period.incomeTotal);
    this.expenseTotal.set(data.period.expenseTotal);
    this.netCashFlow.set(data.period.netCashFlow);
    this.netCashFlowAbs.set(Math.abs(data.period.netCashFlow));
    this.totalCategoryAmount.set(data.period.categories.reduce((sum, category) => sum + category.amount, 0));
    this.totalCreditLimit.set(data.current.creditLimit);
    this.totalCreditUsed.set(data.current.creditUsed);
    this.percentajeCreditUsed.set(data.current.creditLimit > 0 ? (data.current.creditUsed / data.current.creditLimit) * 100 : 0);
    
    // First 4 categories for the chart, the rest will be shown in the list
    const topCategories = data.period.categories.slice(0, 4);
    const otherCategoriesAmount = data.period.categories.slice(4).reduce((sum, category) => sum + category.amount, 0);
    if (otherCategoriesAmount > 0) {
      topCategories.push({
        resourceCode: 'others',
        name: 'Otros',
        amount: otherCategoriesAmount,
        color: '#3f3f3f' // Color gris claro para la categoría "Otros"
      } as CategoryResponseDTO);
    }

    // First 2 credit cards for the dashboard, the rest will be shown in the list
    const topCreditCards = data.current.creditCards.slice(0, 2);
    const otherCreditCardsAmount = data.current.creditCards.slice(2).reduce((sum, card) => sum + card.currentBalance, 0);
    const otherCreditLimitAmount = data.current.creditCards.slice(2).reduce((sum, card) => sum + card.creditLimit, 0);
    if (otherCreditLimitAmount > 0) {
      topCreditCards.push({
        resourceCode: 'others',
        name: 'Otros',
        creditLimit: otherCreditLimitAmount,
        currentBalance: otherCreditCardsAmount,
      } as CreditCardResponseDTO);
    }

    this.categories.set(data.period.categories.length <= 5 ? data.period.categories : topCategories);
    this.creditCards.set(data.current.creditCards.length <= 3 ? data.current.creditCards : topCreditCards);
    this.transactions.set(data.period.lastTransactions);

    // Esperar a que el DOM se actualice antes de intentar renderizar el gráfico
    if (this.categories().length > 0 && isPlatformBrowser(this.platformId)) {
      setTimeout(() => {
        this.updateExpenseChart();
      }, 100);
    }
  }

  private loadDashboardData(): void {
    this.dashboardState.isLoadingDashboardData.set(true);
    this.dashboardState.errorDashboardData.set(null);

    this.dashboardService.generateDashboardData(this.selectedPeriod()).subscribe({
      next: (data: ApiResponseDTO<DashboardResponseModelDTO>) => {
        this.dashboardState.isLoadingDashboardData.set(false);
        if (data.success && data.data) {
          this.dashboardData.set(data.data);
          this.updateDashboardData();
        } else {
          this.dashboardState.errorDashboardData.set('No se pudo cargar los datos del dashboard.');
        }
      },
      error: (error) => {
        this.dashboardState.isLoadingDashboardData.set(false);
        if (error.status === 500 || error.status === 0) {
          this.dashboardState.errorDashboardData.set('Error del servidor. Por favor, inténtalo de nuevo más tarde.');
        } else {
          this.dashboardState.errorDashboardData.set(error.error.message || 'Ocurrió un error al cargar los datos del dashboard.');
        }
      }
    });
  }

  private updateExpenseChart(): void {
    // Verificar que estemos en el navegador y que Chart.js esté registrado
    if (!isPlatformBrowser(this.platformId) || !this.chartRegistered) {
      return;
    }

    const canvas = document.getElementById('expenseChart') as HTMLCanvasElement;
    if (!canvas) {
      // Si el canvas aún no está disponible, reintentar después de un tiempo
      setTimeout(() => {
        const retryCanvas = document.getElementById('expenseChart') as HTMLCanvasElement;
        if (retryCanvas) {
          this.renderChart(retryCanvas);
        }
      }, 200);
      return;
    }

    this.renderChart(canvas);
  }

  private renderChart(canvas: HTMLCanvasElement): void {
    const ctx = canvas.getContext('2d');
    if (!ctx) {
      console.warn('Canvas context not available');
      return;
    }

    const categories = this.categories();
    
    // Verificar que haya categorías para mostrar
    if (categories.length === 0) {
      if (this.expenseChart) {
        this.expenseChart.destroy();
        this.expenseChart = null;
      }
      return;
    }
    
    // Destruir el gráfico existente antes de crear uno nuevo
    if (this.expenseChart) {
      this.expenseChart.destroy();
      this.expenseChart = null;
    }

    // Crear el nuevo gráfico
    const config: ChartConfiguration<'doughnut'> = {
        type: 'doughnut',
        data: {
          labels: categories.map(c => c.name),
          datasets: [{
            data: categories.map(c => c.amount),
            backgroundColor: categories.map(c => c.color),
            borderWidth: 0,
            borderRadius: 4,
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: true,
          cutout: '70%',
          animation: {
            duration: 500,
            easing: 'easeInOutQuart'
          },
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              backgroundColor: '#FFFFFF',
              titleColor: '#212529',
              bodyColor: '#6C757D',
              borderColor: '#DEE2E6',
              borderWidth: 1,
              padding: 12,
              boxPadding: 6,
              usePointStyle: true,
              callbacks: {
                label: (context) => {
                  const value = context.parsed;
                  return ` PEN ${value.toLocaleString('es-ES', { minimumFractionDigits: 2 })}`;
                }
              }
            }
          }
        }
      };

    this.expenseChart = new Chart(ctx, config);
  }
}
