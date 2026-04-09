import { Component, input } from '@angular/core';

@Component({
  selector: 'app-loading-ui',
  imports: [],
  templateUrl: './loading.ui.html',
  styleUrl: './loading.ui.css',
})
export class LoadingUi {
  cssClasses = input<string>('py-16');
}
