package org.xpenbox.payment.provider.mercadopago.mapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPAutoRecurring;

import jakarta.inject.Singleton;

/**
 * MPMapper is a utility class responsible for mapping between the generic ProviderPlanRequestDTO used in the application and the specific MPApprovalPlanRequestDTO required by the MercadoPago API. This class encapsulates the logic for converting data from the application's internal representation to the format expected by MercadoPago, including handling any necessary transformations or calculations related to billing cycles, amounts, and URLs.
 */
@Singleton
public class MPMapper {
    private static final Logger LOG = Logger.getLogger(MPMapper.class);

    @ConfigProperty(name = "payment.success.url")
    private String paymentSuccessUrl;

    /**
     * Maps a ProviderPlanRequestDTO to an MPApprovalPlanRequestDTO, which is the format required by the MercadoPago API for creating a subscription (pre-approval plan). This method extracts relevant information from the ProviderPlanRequestDTO, such as the plan name, user details, billing cycle, amount, and currency, and constructs an MPApprovalPlanRequestDTO that can be sent to the MercadoPago API.
     * @param subscriptionPlanRequestDTO
     * @return
     */
    public MPApprovalSubscriptionRequestDTO toMPApprovalSubscriptionRequestDTO(ProviderSubscriptionRequestDTO subscriptionPlanRequestDTO) {
        LOG.debugf("Mapping ProviderPlanRequestDTO to MPApprovalSubscriptionRequestDTO: %s", subscriptionPlanRequestDTO);

        return new MPApprovalSubscriptionRequestDTO(
            subscriptionPlanRequestDTO.planName(),
            subscriptionPlanRequestDTO.userId().toString(),
            subscriptionPlanRequestDTO.userEmail(),
            new MPAutoRecurring(
                subscriptionPlanRequestDTO.billingCycle().getFrecuencyValue(),
                subscriptionPlanRequestDTO.billingCycle().getFrecuencyName(),
                subscriptionPlanRequestDTO.amount(),
                subscriptionPlanRequestDTO.currency()
            ),
            paymentSuccessUrl
        );
    }

    public ProviderSubscriptionResponseDTO toProviderSubscriptionResponseDTO(MPApprovalSubscriptionResponseDTO mpApprovalSubscriptionResponseDTO) {
        LOG.debugf("Mapping MPApprovalSubscriptionResponseDTO to ProviderSubscriptionResponseDTO: %s", mpApprovalSubscriptionResponseDTO);

        return new ProviderSubscriptionResponseDTO(
            mpApprovalSubscriptionResponseDTO.id(),
            mpApprovalSubscriptionResponseDTO.init_point(),
            mpApprovalSubscriptionResponseDTO.status(),
            PaymentProviderType.MERCADOPAGO
        );
    }
}
