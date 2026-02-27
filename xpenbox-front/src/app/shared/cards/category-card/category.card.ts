import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { categoryState } from '../../../feature/category/service/category.state';

@Component({
  selector: 'app-category-card',
  imports: [CommonModule],
  templateUrl: './category.card.html',
  styleUrl: './category.card.css',
  host: {
    'class': 'block',
    'ngSkipHydration': 'true'
  }
})
export class CategoryCard {

  categoryCardResourceCode = input<string>();
  categoryCardName = input<string>();
  categoryCardColor = input<string>();
  categoryCardState = input<boolean>();
  categoryTotalUses = input<number>();

  categoryCardEdit = output<string>();
  categoryCardChangeState = output<string>();

  onEditCategory() {
    categoryState.errorSendingCategory.set(null);
    this.categoryCardEdit.emit(this.categoryCardResourceCode() || '');
  }

  onDisableCategory() {
    categoryState.errorSendingCategory.set(null);
    this.categoryCardChangeState.emit(this.categoryCardResourceCode() || '');
  }
}
