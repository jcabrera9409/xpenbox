package org.xpenbox.payment.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.SubscriptionPayment;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.dto.ProviderPaymentResponseDTO;

import jakarta.inject.Singleton;

/**
 * SubscriptionPaymentMapper is a utility class responsible for mapping between the generic ProviderPaymentResponseDTO received from payment providers and the SubscriptionPayment entity used in the application's database. This class encapsulates the logic for converting data from the payment provider's response into a format that can be persisted in the database, including generating unique resource codes, setting provider details, and handling date conversions. By centralizing this mapping logic, the SubscriptionPaymentMapper promotes code reuse and maintainability when dealing with payment data across different providers.
 */
@Singleton
public class SubscriptionPaymentMapper {
    private static final Logger LOG = Logger.getLogger(SubscriptionPaymentMapper.class.getName());

    /**
     * Maps a ProviderPaymentResponseDTO to a SubscriptionPayment entity. This method takes the payment response from the payment provider, along with the associated subscription and provider type, and creates a new SubscriptionPayment entity that can be persisted in the database. The mapping includes generating a unique resource code for the payment, setting the provider details, amount, currency, payment date, and status based on the information provided in the payment response.
     * @param paymentResponse the response from the payment provider containing details about the payment, such as the payment ID, amount, currency, approval date, and status
     * @param subscription the Subscription entity associated with the payment, which links the payment to a specific subscription in the system
     * @param providerType the type of payment provider (e.g., Stripe, PayPal, MercadoPago) that processed the payment, which is used to set the provider field in the SubscriptionPayment entity
     * @return a SubscriptionPayment entity populated with the details from the payment response and associated subscription, ready to be persisted in the database
     */
    public SubscriptionPayment toEntity(ProviderPaymentResponseDTO paymentResponse, Subscription subscription, PaymentProviderType providerType) {
        LOG.debugf("Mapping ProviderPaymentResponseDTO to SubscriptionPayment entity: %s", paymentResponse);
        SubscriptionPayment entity = new SubscriptionPayment();
        entity.setResourceCode(ResourceCode.generateSubscriptionPaymentResourceCode());
        entity.setSubscription(subscription);
        entity.setProvider(providerType.name());
        entity.setProviderPaymentId(paymentResponse.id().toString());
        entity.setAmount(paymentResponse.amount());
        entity.setCurrency(paymentResponse.currency());
        entity.setPaymentDate(paymentResponse.dateApproved());
        entity.setStatus(paymentResponse.status());

        return entity;
    }
}
