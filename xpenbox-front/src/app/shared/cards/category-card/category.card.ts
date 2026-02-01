import { Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

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
    this.categoryCardEdit.emit(this.categoryCardResourceCode() || '');
  }

  onDisableCategory() {
    this.categoryCardChangeState.emit(this.categoryCardResourceCode() || '');
  }
}
