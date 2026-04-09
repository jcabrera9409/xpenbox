import { Component, HostListener, input, output } from '@angular/core';

@Component({
  selector: 'app-tooltip-ui',
  imports: [],
  templateUrl: './tooltip.ui.html',
  styleUrl: './tooltip.ui.css',
})
export class TooltipUi {
  showTooltip = input<boolean>(false);
  message = input<string>('');

  showTooltipOutput = output<boolean>();

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: Event): void {
    if (this.showTooltip()) {
      this.showTooltipOutput.emit(false);
    }
  }
}
