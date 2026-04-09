import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-retry-component',
  imports: [],
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
