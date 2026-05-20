import { Component } from '@angular/core';
import { upgradeProModalState } from '../state/upgrade-pro.modal.state';
import { subscriptionState } from '../../../feature/subscription/service/subscription.state';
import { SubscriptionService } from '../../../feature/subscription/service/subscription.service';
import { PreApprovalSubscriptionRequestDTO } from '../../../feature/subscription/model/pre-approval-subscription.request.dto';
import { PreApprovalSubscriptionResponseDTO } from '../../../feature/subscription/model/pre-approval-subscription.response.dto';
import { ApiResponseDTO } from '../../../feature/common/model/api.response.dto';
import { IconComponent } from '../../../shared/components/icon.component/icon.component';
import { GenericModal } from '../../common/generic-modal/generic.modal';

@Component({
  selector: 'app-upgrade-pro-modal',
  imports: [IconComponent, GenericModal],
  templateUrl: './upgrade-pro.modal.html',
  styleUrl: './upgrade-pro.modal.css',
})
export class UpgradeProModal {

  title = upgradeProModalState.title;
  htmlMessage = upgradeProModalState.htmlMessage;

  subscriptionState = subscriptionState;

  constructor(
    private subscriptionService: SubscriptionService
  ) {
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
