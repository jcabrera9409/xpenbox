import { Component, input, output } from '@angular/core';
import { IconComponent } from '../icon.component/icon.component';

@Component({
  selector: 'app-create-first-component',
  imports: [IconComponent],
  templateUrl: './create-first.component.html',
  styleUrl: './create-first.component.css',
})
export class CreateFirstComponent {

  icon = input<string>();
  title = input<string>();
  subtitle = input<string>();
  buttonText = input<string | undefined>();
  
  create = output<void>();

  onCreate() {
    this.create.emit();
  }
}
