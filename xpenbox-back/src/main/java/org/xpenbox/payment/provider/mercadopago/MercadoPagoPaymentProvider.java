package org.xpenbox.payment.provider.mercadopago;

import java.math.BigDecimal;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.xpenbox.payment.dto.SubscriptionRequestDTO;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;
import org.xpenbox.payment.provider.PaymentProvider;
import org.xpenbox.payment.provider.mercadopago.client.MercadoPagoClient;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalResponseDTO;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the PaymentProvider interface for MercadoPago. This class provides specific logic for interacting with the MercadoPago API to manage subscriptions and handle webhooks.
 */
@ApplicationScoped
public class MercadoPagoPaymentProvider implements PaymentProvider {
    private static final Logger LOG = Logger.getLogger(MercadoPagoPaymentProvider.class);

    @ConfigProperty(name = "payment.success.url")
    String paymentSuccessUrl;

    private final MercadoPagoClient mercadoPagoClient;

    public MercadoPagoPaymentProvider(@RestClient MercadoPagoClient mercadoPagoClient) {
        this.mercadoPagoClient = mercadoPagoClient;
    }

    @Override
    public SubscriptionResponseDTO createPreApprovalPlan(SubscriptionRequestDTO subscriptionRequest) {
        LOG.infof("Creating pre-approval plan for user %s with plan %s using MercadoPago", subscriptionRequest.userEmail(), subscriptionRequest.planName());

        MPApprovalRequestDTO mpApprovalRequest = mapToMPApprovalRequestDTO(subscriptionRequest);
        LOG.debugf("Mapped SubscriptionRequestDTO to MPApprovalRequestDTO: %s", mpApprovalRequest);

        MPApprovalResponseDTO mpApprovalResponse = mercadoPagoClient.createSubscription(mpApprovalRequest);
        
        LOG.debugf("Received MPApprovalResponseDTO from MercadoPago: %s", mpApprovalResponse);
        return mapToSubscriptionResponseDTO(mpApprovalResponse);
    }

    @Override
    public void handleWebhook(String payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleWebhook'");
    }

    private MPApprovalRequestDTO mapToMPApprovalRequestDTO(SubscriptionRequestDTO subscriptionRequest) {
        LOG.debugf("Mapping SubscriptionRequestDTO to MPApprovalRequestDTO: %s", subscriptionRequest);

        String reason = subscriptionRequest.planName();
        String externalReference = subscriptionRequest.userId().toString();
        String payerEmail = subscriptionRequest.userEmail();
        String backUrl = paymentSuccessUrl;

        BigDecimal amount = subscriptionRequest.amount();
        String currency = subscriptionRequest.currency();

        return new MPApprovalRequestDTO(
            reason,
            externalReference,
            payerEmail,
            MPApprovalRequestDTO.generateMonthlyAutoRecurring(amount, currency),
            backUrl
        );
    }

    private SubscriptionResponseDTO mapToSubscriptionResponseDTO(MPApprovalResponseDTO mpApprovalResponse) {
        LOG.debugf("Mapping MPApprovalResponseDTO to SubscriptionResponseDTO: %s", mpApprovalResponse);

        String providerSubscriptionId = mpApprovalResponse.id();
        String approvalUrl = mpApprovalResponse.init_point();
        String status = mpApprovalResponse.status();

        return new SubscriptionResponseDTO(
            providerSubscriptionId,
            approvalUrl,
            status
        );
    }
}
