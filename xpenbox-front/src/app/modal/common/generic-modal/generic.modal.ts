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

  iconHeader = input<string>('');
  classIconHeader = input<string>('');

  title = input<string>('');
  subtitle = input<string>('');

  isLoading = input<boolean>(false);
  isFormValid = input<boolean>(false);
  formId = input<string>('');
  confirmText = input<string>('Confirmar');
  confirmColor = input<string>('var(--xpb-primary)');
  confirmTextLoading = input<string>('Guardando...');
  confirmIcon = input<string>('add');

  confirm = output<void>();
  close = output<void>();

  constructor() {
    super();
  }

  override ngOnInit(): void {
    super.ngOnInit();
  }
}
