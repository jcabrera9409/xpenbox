import { Component, input } from '@angular/core';

@Component({
  selector: 'app-tab-content-ui',
  imports: [],
  templateUrl: './tab-content.ui.html',
  styleUrl: './tab-content.ui.css',
})
export class TabContentUi {
  animationDirection = input<'left' | 'right'>('right');
}
