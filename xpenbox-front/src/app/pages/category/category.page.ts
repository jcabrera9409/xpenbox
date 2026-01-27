import { Component, computed, inject, PLATFORM_ID, signal } from '@angular/core';
import { CommonModule, isPlatformServer } from '@angular/common';
import { CategoryService } from '../../feature/category/service/category.service';
import { categoryState } from '../../feature/category/service/category.state';
import { CategoryCard } from '../../shared/cards/category-card/category.card';
import { CategoryEditionModal } from '../../modal/category/category-edition-modal/category-edition.modal';
import { SummaryCard } from '../../shared/cards/summary-card/summary.card';
import { LoadingUi } from '../../shared/ui/loading-ui/loading.ui';

@Component({
  selector: 'app-category-page',
  imports: [CommonModule, CategoryCard, CategoryEditionModal, SummaryCard, LoadingUi],
  templateUrl: './category.page.html',
  styleUrl: './category.page.css',
})
export class CategoryPage {

  categoryState = categoryState;

  showCategoryEditionModal = signal(false);
  resourceCodeCategorySelected = signal<string | null>(null);

  // Computed signal for active categories count
  activeCategoriesCount = computed(() => 
    this.categoryState.categories().filter(c => c.state).length
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

  closeCategoryEditionModal() {
    this.showCategoryEditionModal.set(false);
  }

  reloadCategories() {
    this.categoryService.refresh();
  }
}
