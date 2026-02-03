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

@Component({
  selector: 'app-category-page',
  imports: [CommonModule, CategoryCard, CategoryEditionModal, SummaryCard, LoadingUi, RetryComponent, CreateFirstComponent, ConfirmModal],
  templateUrl: './category.page.html',
  styleUrl: './category.page.css',
})
export class CategoryPage {

  categoryState = categoryState;

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
    private categoryService: CategoryService
  ) {
    const platformId = inject(PLATFORM_ID);
    if (isPlatformServer(platformId)) {
      return;
    }

    this.categoryService.load();
  }

  openCategoryEditionModal(resourceCodeCategorySelected: string | null = null) {
    this.resourceCodeCategorySelected.set(resourceCodeCategorySelected);
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

    const categoryData = new CategoryRequestDTO(
      undefined!,
      undefined!,
      this.categoryDataSelected()!.state ? false : true
    );

    this.categoryService.update(resourceCode, categoryData).subscribe({
      next: () => {
        this.categoryState.isLoadingSendingCategory.set(false);
        this.showConfirmModal.set(false);
        this.categoryService.refresh();
      },
      error: (error) => {
        if (error.status === 500 || error.status === 0) {
          this.categoryState.errorSendingCategory.set('Ocurrió un error al actualizar la categoría. Por favor, intenta nuevamente.');
        } else {
          this.categoryState.errorSendingCategory.set(error.error.message || 'Ocurrió un error al actualizar la categoría. Por favor, intenta nuevamente.');
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

  private updateNewStateDataModal() {
    if (!this.categoryDataSelected()) {
      this.titleConfirmModal.set(null);
      this.messageConfirmModal.set(null);
      this.confirmTextConfirmModal.set(null);
      return;
    }

    const isActive = this.categoryDataSelected()!.state;
    
    if (isActive) {
      this.titleConfirmModal.set('Inhabilitar Categoría');
      this.messageConfirmModal.set(`¿Estás seguro de que deseas inhabilitar la categoría "${this.categoryDataSelected()!.name}"?`);
      this.confirmTextConfirmModal.set('Inhabilitar');
    } else {
      this.titleConfirmModal.set('Habilitar Categoría');
      this.messageConfirmModal.set(`¿Estás seguro de que deseas habilitar la categoría "${this.categoryDataSelected()!.name}"?`);
      this.confirmTextConfirmModal.set('Habilitar');
    }
  }

  private loadCategoryData() {
    if (!this.resourceCodeCategorySelected()) return;

    this.categoryState.isLoadingGetCategory.set(true);

    this.categoryService.getByResourceCode(this.resourceCodeCategorySelected()!).subscribe({
      next: (data: ApiResponseDTO<CategoryResponseDTO>) => {
        this.categoryDataSelected.set(data.data);
        this.updateNewStateDataModal();
        this.categoryState.isLoadingGetCategory.set(false);
        console.log('Category data loaded:', data.data);
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
