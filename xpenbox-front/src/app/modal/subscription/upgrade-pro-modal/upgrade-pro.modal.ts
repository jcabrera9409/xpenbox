import { Component, output, OnInit, OnDestroy, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser, DOCUMENT } from '@angular/common';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { ModalGeneric } from '../../common/modal.generic';

@Component({
  selector: 'app-upgrade-pro-modal',
  imports: [ModalButtonsUi],
  templateUrl: './upgrade-pro.modal.html',
  styleUrl: './upgrade-pro.modal.css',
})
export class UpgradeProModal extends ModalGeneric {

  // Outputs para comunicar acciones al componente padre
  close = output<void>();
  upgrade = output<void>();

  /**
   * Cierra el modal sin realizar ninguna acción
   */
  onClose(): void {
    this.close.emit();
  }

  /**
   * Emite el evento de actualización para iniciar el proceso de suscripción
   */
  onUpgrade(): void {
    this.upgrade.emit();
  }
}
