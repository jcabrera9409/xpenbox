import { Component, input, output } from '@angular/core';
import { IconComponent } from '../icon.component/icon.component';

@Component({
  selector: 'app-retry-component',
  imports: [IconComponent],
  templateUrl: './retry.component.html',
  styleUrl: './retry.component.css',
})
export class RetryComponent {
  message = input<string>('Ocurrió un error inesperado.');
  retry = output<void>();

  sendRetry() {
    this.retry.emit();
  }
}
