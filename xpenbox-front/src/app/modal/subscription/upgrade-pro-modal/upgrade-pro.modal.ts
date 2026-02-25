import { Component, output, OnInit, OnDestroy, inject, PLATFORM_ID, input } from '@angular/core';
import { isPlatformBrowser, DOCUMENT } from '@angular/common';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { ModalGeneric } from '../../common/modal.generic';
import { upgradeProModalState } from '../state/upgrade-pro.modal.state';

@Component({
  selector: 'app-upgrade-pro-modal',
  imports: [ModalButtonsUi],
  templateUrl: './upgrade-pro.modal.html',
  styleUrl: './upgrade-pro.modal.css',
})
export class UpgradeProModal extends ModalGeneric {

  title = upgradeProModalState.title;
  htmlMessage = upgradeProModalState.htmlMessage;

  onClose(): void {
    upgradeProModalState.open.set(false);
    upgradeProModalState.title.set(null);
    upgradeProModalState.htmlMessage.set(null);
  }

  onUpgrade(): void {
    console.log('Upgrade to Pro clicked');
  }
}
