import { Component, computed, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { CategoryService } from '../../feature/category/service/category.service';
import { categoryState } from '../../feature/category/service/category.state';
import { CategoryCard } from '../../shared/cards/category-card/category.card';
import { CategoryEditionModal } from '../../modal/category/category-edition-modal/category-edition.modal';
import { SummaryCard } from '../../shared/cards/summary-card/summary.card';
import { LoadingUi } from '../../shared/ui/loading-ui/loading.ui';
import { RetryComponent } from '../../shared/components/retry-component/retry.component';
import { CreateFirstComponent } from '../../shared/components/create-first-component/create-first.component';
import { ConfirmModal } from '../../modal/common/confirm-modal/confirm.modal';
import { CategoryResponseDTO } from '../../feature/category/model/category.response.dto';
import { ApiResponseDTO } from '../../feature/common/model/api.response.dto';
import { CategoryRequestDTO } from '../../feature/category/model/category.request.dto';
import { NotificationService } from '../../feature/common/service/notification.service';
import { IconComponent } from '../../shared/components/icon.component/icon.component';
import { CategoryBudgetUsageRequestDTO } from '../../feature/category/model/categorybudgetusage.request.dto';
import { userState } from '../../feature/user/service/user.state';

@Component({
  selector: 'app-category-page',
  imports: [CommonModule, CategoryCard, CategoryEditionModal, SummaryCard, LoadingUi, RetryComponent, CreateFirstComponent, ConfirmModal, IconComponent],
  templateUrl: './category.page.html',
  styleUrl: './category.page.css',
})
export class CategoryPage {

  categoryState = categoryState;
  userState = userState;

  showCategoryEditionModal = signal(false);
  resourceCodeCategorySelected = signal<string | null>(null);

  showConfirmModal = signal(false);
  titleConfirmModal = signal<string | null>(null);
  messageConfirmModal = signal<string | null>(null);
  confirmTextConfirmModal = signal<string | null>(null);

  categoryDataSelected = signal<CategoryResponseDTO | null>(null);
  
  // Computed signal for active categories count
  activeCategoriesCount = computed(() => 
    this.categoryState.categories().filter(c => c.state).length
  );

  // Computed signal for ordered categories by name and active state
  orderedCategories = computed(() => 
    [...this.categoryState.categories()].sort((a, b) => {
      if (a.state === b.state) {
        return a.name.localeCompare(b.name);
      }
      return a.state ? -1 : 1;
    })
  );

  constructor(
    private categoryService: CategoryService,
    private notificationService: NotificationService
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.categoryService.load();
    this.categoryService.loadBudgetUsage();
  }

  get totalBudgetString(): string {
    return `${this.currency} ${this.totalBudget.toFixed(2)}`;
  }

  get totalBudget(): number {
    return this.categoryState.categories()
      .filter(category => category.hasBudget && category.state)
      .reduce((total, category) => {
        return total + category.budget;
    }, 0);
  }

  get totalBudgetRemainingString(): string {
    const remaining = this.totalBudget - this.totalBudgetUsed;
    return `${this.currency} ${remaining.toFixed(2)}`;
  }

  get totalBudgetUsed(): number {
    return this.categoryState.categoriesBudgetUsage()
    ?.filter(usage => usage.category.hasBudget && usage.category.state)
    .reduce((total, usage) => {
        return total + usage.budgetUsed;
    }, 0) || 0;
  }

  get percentajeBudgetUsed(): number {
    const totalBudget = this.totalBudget;
    if (totalBudget === 0) {
      return 0;
    }
    return (this.totalBudgetUsed / totalBudget) * 100;
  }

  get percentajeBudgetUsedString(): string {
    const percentaje = this.percentajeBudgetUsed;
    return `${percentaje.toFixed(0)}%`;
  }

  get countCategoriesWithBudget(): number {
    return this.categoryState.categories().filter(category => category.hasBudget && category.state).length;
  }

  get currency(): string {
    return this.userState.userLogged()?.currency || '';
  }

  get barColor(): string {
    if (this.totalBudget === 0) {
      return 'xpb-text-primary';
    }
    if (this.percentajeBudgetUsed < 50) {
      return 'xpb-text-success';
    } else if (this.percentajeBudgetUsed < 100) {
      return 'xpb-text-warning';
    } else {
      return 'xpb-text-error';
    }
  }

  openCategoryEditionModal(resourceCodeCategorySelected: string | null = null) {
    this.resourceCodeCategorySelected.set(resourceCodeCategorySelected);
    categoryState.errorSendingCategory.set(null);
    this.showCategoryEditionModal.set(true);
  }

  openConfirmNewStateModal(resourceCodeCategorySelected: string | null = null) {
    this.resourceCodeCategorySelected.set(resourceCodeCategorySelected);
    this.loadCategoryData();
    this.showConfirmModal.set(true);
  }

  confirmNewStateCategory(resourceCode: string) { 
    if (!this.categoryDataSelected()) return;

    this.categoryState.isLoadingSendingCategory.set(true);

    this.categoryService.delete(resourceCode).subscribe({
      next: () => {
        this.categoryState.isLoadingSendingCategory.set(false);
        this.showConfirmModal.set(false);
        this.categoryService.refresh();
        this.notificationService.success('Categoría eliminada correctamente.');
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.categoryState.errorSendingCategory.set('Ocurrió un error al eliminar la categoría. Por favor, intenta nuevamente.');
        } else {
          this.categoryState.errorSendingCategory.set(error.error.message || 'Ocurrió un error al eliminar la categoría. Por favor, intenta nuevamente.');
        }
        this.categoryState.isLoadingSendingCategory.set(false);
      }
    });
  }

  closeConfirmNewStateModal() {
    this.showConfirmModal.set(false);
    this.categoryState.errorSendingCategory.set(null);
  }

  closeCategoryEditionModal() {
    this.showCategoryEditionModal.set(false);
  }

  reloadCategories() {
    this.categoryService.refresh();
  }

  getCategoryBudgetUsage(resourceCode: string): CategoryBudgetUsageRequestDTO | undefined {
    const budgetUsage = this.categoryState.categoriesBudgetUsage()?.find(usage => usage.category.resourceCode === resourceCode);
    return budgetUsage;
  }

  private updateNewStateDataModal() {
    if (!this.categoryDataSelected()) {
      this.titleConfirmModal.set(null);
      this.messageConfirmModal.set(null);
      this.confirmTextConfirmModal.set(null);
      return;
    }
    
    this.titleConfirmModal.set('Eliminar Categoría');
    this.messageConfirmModal.set(`<p>¿Estás seguro de que deseas eliminar la categoría "${this.categoryDataSelected()!.name}"?</p><p>Esta acción dejará a las transacciones asociadas sin categoría.</p>`);
    this.confirmTextConfirmModal.set('Eliminar');
  }

  private loadCategoryData() {
    if (!this.resourceCodeCategorySelected()) return;

    this.categoryState.isLoadingGetCategory.set(true);

    this.categoryService.getByResourceCode(this.resourceCodeCategorySelected()!).subscribe({
      next: (data: ApiResponseDTO<CategoryResponseDTO>) => {
        this.categoryDataSelected.set(data.data);
        this.updateNewStateDataModal();
        this.categoryState.isLoadingGetCategory.set(false);
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.categoryState.errorGetCategory.set('Ocurrió un error al validar la categoría. Por favor, intenta nuevamente.');
        } else {
          this.categoryState.errorGetCategory.set(error.error.message || 'Ocurrió un error al validar la categoría. Por favor, intenta nuevamente.');
        }
        this.categoryState.isLoadingGetCategory.set(false);
      }
    });
  }
}
