import { Component, effect, input, output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoadingUi } from '../../ui/loading-ui/loading.ui';
import { categoryState } from '../../../feature/category/service/category.state';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { CategoryService } from '../../../feature/category/service/category.service';

@Component({
  selector: 'app-categories-carousel-component',
  imports: [CommonModule, LoadingUi],
  templateUrl: './categories-carousel.component.html',
  styleUrl: './categories-carousel.component.css',
})
export class CategoriesCarouselComponent {

  title = input<string>('Categoría');
  outputCategory = output<CategoryResponseDTO>();

  categoryState = categoryState;

  selectedCategory = signal<CategoryResponseDTO | null>(null);
  categoriesList = signal<CategoryResponseDTO[]>([]);

  constructor(
    private categoryService: CategoryService
  ) {
    if (this.categoryState.categories().length === 0) {
      this.categoryService.load();
    }

    // Auto-select first category when loaded
    effect(() => {
      const categories = this.categoryState.categories().filter(c => c.state);
      if (categories.length > 0 && !this.selectedCategory()) {
        const finalOrder = this.filterAndSortCategories([...categories]);
        this.categoriesList.set(finalOrder);
        this.selectCategory(finalOrder[0] || null);
      }
    });
  }

  // Getters for filtered and sorted lists
  get categories(): CategoryResponseDTO[] {
    return this.categoryState.categories().filter(c => c.state);
  }

  retryLoadCategories(): void {
    this.categoryService.refresh();
  }

  /**
   * Select a category
   * @param category The category to select
   * @returns void
   */
  selectCategory(category: CategoryResponseDTO): void {
    this.selectedCategory.set(category);
    this.outputCategory.emit(category);
  }

  /**
   * Check if a category is selected
   * @param categoryResourceCode The resource code of the category to check
   * @returns boolean
   */
  isSelectedCategory(categoryResourceCode: string): boolean {
    return this.selectedCategory()?.resourceCode === categoryResourceCode;
  }

  /**
   * Filter and sort categories to have the two most recently used at the top,
   * followed by the rest sorted by usage count.
   * @param categories The list of categories to filter and sort.
   * @returns The filtered and sorted list of categories.
   */
  private filterAndSortCategories(categories: CategoryResponseDTO[]): CategoryResponseDTO[] {
    const sortedByLastUsed = [...categories]
      .filter(c => c.lastUsedDateTimestamp)
      .sort((a, b) => (b.lastUsedDateTimestamp || 0) - (a.lastUsedDateTimestamp || 0));

    const lastTwo = sortedByLastUsed.slice(0, 2);
    const lastTwoIds = new Set(lastTwo.map(c => c.resourceCode));

    const rest = categories
      .filter(c => !lastTwoIds.has(c.resourceCode))
      .sort((a, b) => b.usageCount - a.usageCount);

    return [...lastTwo, ...rest];
  }
}
