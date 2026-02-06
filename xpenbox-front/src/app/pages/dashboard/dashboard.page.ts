import { Component, OnInit, AfterViewInit, signal, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

@Component({
  selector: 'app-dashboard-page',
  imports: [CommonModule],
  templateUrl: './dashboard.page.html',
  styleUrl: './dashboard.page.css',
})
export class DashboardPage implements OnInit, AfterViewInit {
  private platformId = inject(PLATFORM_ID);
  private expenseChart: Chart | null = null;

  // Datos mock del dashboard
  selectedPeriod = signal('Mes Actual');
  generalBalance = signal(45230.00);
  balanceChange = signal(12.4);
  balanceChangeAmount = signal(3150.00);
  totalResults = signal(8400.00);
  totalIncome = signal(8400.00);
  totalExpenses = signal(5250.00);

  // Tarjetas de crédito
  creditCards = signal([
    {
      name: 'Mastercard Platinum',
      totalDebt: 2840.50,
      progress: 75
    },
    {
      name: 'Visa Infinite',
      totalDebt: 0,
      progress: 12
    },
    {
      name: 'Otros',
      totalDebt: 0,
      progress: 12
    }
  ]);

  // Datos para el gráfico de gastos por categoría
  expenseCategories = signal([
    { name: 'Alimentación', amount: 1837, percentage: 35, color: '#4361EE' },
    { name: 'Vivienda', amount: 1312, percentage: 25, color: '#BC4749' },
    { name: 'Transporte', amount: 1050, percentage: 20, color: '#588157' },
    { name: 'Educación', amount: 1050, percentage: 20, color: '#FFD700' },
    { name: 'Otros', amount: 1051, percentage: 20, color: '#6C757D' }
  ]);

  // Transacciones recientes
  recentTransactions = signal([
    { detail: 'Supermercado El Ahorro', category: 'Alimentación', date: '2026-02-04', amount: -45.50 },
    { detail: 'Salario Enero', category: 'Ingreso', date: '2026-02-01', amount: 3500.00 },
    { detail: 'Netflix Suscripción', category: 'Entretenimiento', date: '2026-01-30', amount: -12.99 },
    { detail: 'Gasolina Shell', category: 'Transporte', date: '2026-01-28', amount: -60.00 }
  ]);

  ngOnInit(): void {
    // Inicialización si es necesaria
  }

  ngAfterViewInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      Chart.register(...registerables);
      this.createExpenseChart();
    }
  }

  ngOnDestroy(): void {
    if (this.expenseChart) {
      this.expenseChart.destroy();
    }
  }

  private createExpenseChart(): void {
    const canvas = document.getElementById('expenseChart') as HTMLCanvasElement;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const categories = this.expenseCategories();
    
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
        animation: false,
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

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'PEN',
      minimumFractionDigits: 2
    }).format(amount);
  }

  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }
}
