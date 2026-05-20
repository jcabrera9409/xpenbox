import { Component, input, output } from '@angular/core';
import { IconComponent } from '../../components/icon.component/icon.component';

@Component({
  selector: 'app-modal-buttons-ui',
  imports: [IconComponent],
  templateUrl: './modal-buttons.ui.html',
  styleUrl: './modal-buttons.ui.css',
})
export class ModalButtonsUi {

  cancelText = input<string>('Cancelar');
  cancelIcon = input<string>('close');

  isEditMode = input<boolean>(false)
  isLoading = input<boolean>(false);
  isValidForm = input<boolean>(false);
  loadingText = input<string>('Guardando...');
  loadingIcon = input<string>('autorenew');
  
  confirmColor = input<string>('var(--xpb-primary)');
  confirmText = input<string>('Registrar');
  confirmIcon = input<string>('add');

  confirmEditText = input<string>('Actualizar');
  confirmEditIcon = input<string>('check');

  responsiveCancelButtonHidden = input<boolean>(false);

  formId = input<string | undefined>(undefined);

  cancel = output<void>();
  confirm = output<void>();

  onCancel() {
    this.cancel.emit();
  }

  onConfirm() {
    this.confirm.emit();
  }
}
