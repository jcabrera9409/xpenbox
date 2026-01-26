import { Component, input } from '@angular/core';

@Component({
  selector: 'app-summary-card',
  imports: [],
  templateUrl: './summary.card.html',
  styleUrl: './summary.card.css',
})
export class SummaryCard {

  isLoading = input<boolean | null>(false);
  titleCard = input<string | null>('');
  contentCard = input<string | null>('');
  contentClassCard = input<string | null>('');
  subtitleCard = input<string | null>('');
  iconCard = input<string | null>('');
}
