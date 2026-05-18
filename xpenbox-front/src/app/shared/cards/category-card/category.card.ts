import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';
import { IconComponent } from '../../components/icon.component/icon.component';
import { CategoryResponseDTO } from '../../../feature/category/model/category.response.dto';
import { CategoryBudgetUsageRequestDTO } from '../../../feature/category/model/categorybudgetusage.request.dto';
import { userState } from '../../../feature/user/service/user.state';

@Component({
  selector: 'app-category-card',
  imports: [CommonModule, IconComponent],
  templateUrl: './category.card.html',
  styleUrl: './category.card.css',
  host: {
    'class': 'block',
    'ngSkipHydration': 'true'
  }
})
export class CategoryCard {

  category = input<CategoryResponseDTO>();
  categoryBudgetUsage = input<CategoryBudgetUsageRequestDTO | undefined>();

  categoryCardEdit = output<string>();
  categoryCardChangeState = output<string>();

  categoryState = categoryState;
  userState = userState;

  onEditCategory() {
    categoryState.errorSendingCategory.set(null);
    this.categoryCardEdit.emit(this.category()?.resourceCode || '');
  }

  onDisableCategory() {
    categoryState.errorSendingCategory.set(null);
    this.categoryCardChangeState.emit(this.category()?.resourceCode || '');
  }

  get currency(): string {
    return this.userState.userLogged()?.currency || '';
  }

  get name(): string {
    return this.category()?.name || '';
  }

  get usageCount(): number {
    return this.categoryBudgetUsage()?.usageCount || 0;
  }

  get state(): boolean {
    return this.category()?.state || false;
  }

  get isLoadCategoryBudgetUsage(): boolean {
    return !this.categoryState.isLoadingGetBudgetUsage() && 
      !this.categoryState.errorGetBudgetUsage() &&
      this.categoryState.categoriesBudgetUsage()?.length !== 0;
  }

  get hasBudget(): boolean {
    return this.category()?.hasBudget || false;
  }

  get budgetUsed(): number {
    if (this.isLoadCategoryBudgetUsage) {
      return this.categoryBudgetUsage()?.budgetUsed || 0;
    }
    return 0;
  }

  get budgetLimit(): number {
    return this.category()?.budget || 0;
  }

  get availableBudget(): number {
    const available = this.budgetLimit - this.budgetUsed;
    return available >= 0 ? available : 0;
  }

  get percentajeBudgetUsed(): number {
    if (!this.isLoadCategoryBudgetUsage) {
      return 0;
    }
    if (this.budgetLimit === 0) {
      return 0;
    }
    return (this.budgetUsed / this.budgetLimit) * 100;
  }

  get barColor(): string {
    if (this.percentajeBudgetUsed < 50) {
      return 'var(--xpb-success)';
    } else if (this.percentajeBudgetUsed < 100) {
      return 'var(--xpb-warning)';
    } else {
      return 'var(--xpb-error)';
    }
  }

}
