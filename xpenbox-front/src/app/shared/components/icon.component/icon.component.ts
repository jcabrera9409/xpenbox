import { Component, input } from '@angular/core';

@Component({
  selector: 'app-icon-component',
  imports: [],
  templateUrl: './icon.component.html',
  styleUrl: './icon.component.css',
})
export class IconComponent {
  icon = input<string>();
  size = input<string>();
  class = input<string>('');

  getWidth(): string {
    if (this.size()) {
      return `w-${this.size()}`;
    }
    return '';
  }

  getHeight(): string {
    if (this.size()) {
      return `h-${this.size()}`;
    }
    return '';
  }
}
