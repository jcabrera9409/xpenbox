import { Component } from '@angular/core';
import { ModalButtonsUi } from '../../../shared/ui/modal-buttons-ui/modal-buttons.ui';
import { ModalGeneric } from '../../common/modal.generic';
import { upgradeProModalState } from '../state/upgrade-pro.modal.state';
import { subscriptionState } from '../../../feature/subscription/service/subscription.state';
import { SubscriptionService } from '../../../feature/subscription/service/subscription.service';
import { PreApprovalSubscriptionRequestDTO } from '../../../feature/subscription/model/pre-approval-subscription.request.dto';
import { PreApprovalSubscriptionResponseDTO } from '../../../feature/subscription/model/pre-approval-subscription.response.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';

@Component({
  selector: 'app-upgrade-pro-modal',
  imports: [ModalButtonsUi],
  templateUrl: './upgrade-pro.modal.html',
  styleUrl: './upgrade-pro.modal.css',
})
export class UpgradeProModal extends ModalGeneric {

  title = upgradeProModalState.title;
  htmlMessage = upgradeProModalState.htmlMessage;

  subscriptionState = subscriptionState;

  constructor(
    private subscriptionService: SubscriptionService
  ) {
    super();
  }

  onClose(): void {
    upgradeProModalState.open.set(false);
    upgradeProModalState.title.set(null);
    upgradeProModalState.htmlMessage.set(null);
  }

  onUpgrade(): void {
    this.subscriptionState.isLoadingSending.set(true);

    const request: PreApprovalSubscriptionRequestDTO = PreApprovalSubscriptionRequestDTO.generateRequestBody();

    this.subscriptionService.createPreApprovalSubscription(request).subscribe({
      next: (response: ApiResponseDTO<PreApprovalSubscriptionResponseDTO>) => {
        window.location.href = response.data.initPointUrl;
      }, error: () => {
        this.subscriptionState.errorSending.set('No se pudo procesar tu pedido. Por favor, intenta nuevamente.');
        this.subscriptionState.isLoadingSending.set(false);
      }
    });
  }
}
