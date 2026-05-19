import { Component, input, OnInit, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { ModalGeneric } from '../modal.generic';

@Component({
  selector: 'app-generic-modal',
  imports: [CommonModule, IconComponent, ModalButtonsUi],
  templateUrl: './generic.modal.html',
  styleUrl: './generic.modal.css',
})
export class GenericModal extends ModalGeneric implements OnInit {

  constructor() {
    super();
  }

  override ngOnInit(): void {
    super.ngOnInit();
  }

  iconHeader = input<string>('');
  classIconHeader = input<string>('');

  title = input<string>('');

  isFormValid = input<boolean>(false);
  isLoading = input<boolean>(false);

  close = output<void>();
}
