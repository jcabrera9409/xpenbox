import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-create-first-component',
  imports: [],
  templateUrl: './create-first.component.html',
  styleUrl: './create-first.component.css',
})
export class CreateFirstComponent {

  icon = input<string>();
  title = input<string>();
  subtitle = input<string>();
  buttonText = input<string>()
  
  create = output<void>();

  onCreate() {
    this.create.emit();
  }
}
