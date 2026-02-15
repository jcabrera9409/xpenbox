package org.xpenbox.payment.provider.mercadopago;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.xpenbox.payment.provider.PaymentProvider;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.MercadoPagoClient;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.mapper.MPMapper;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the PaymentProvider interface for MercadoPago. This class provides specific logic for interacting with the MercadoPago API to manage subscriptions and handle webhooks.
 */
@ApplicationScoped
public class MercadoPagoPaymentProvider implements PaymentProvider {
    private static final Logger LOG = Logger.getLogger(MercadoPagoPaymentProvider.class);

    private final MercadoPagoClient mercadoPagoClient;
    private final MPMapper mpMapper;

    public MercadoPagoPaymentProvider(
        @RestClient MercadoPagoClient mercadoPagoClient,
        MPMapper mpMapper) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.mpMapper = mpMapper;
    }

    @Override
    public ProviderSubscriptionResponseDTO createPreApprovalSubscription(ProviderSubscriptionRequestDTO subscriptionRequest) {
        LOG.infof("Creating pre-approval subscription for user %s with plan %s using MercadoPago", subscriptionRequest.userEmail(), subscriptionRequest.planName());

        MPApprovalSubscriptionRequestDTO mpApprovalRequest = mpMapper.toMPApprovalSubscriptionRequestDTO(subscriptionRequest);
        LOG.debugf("Mapped SubscriptionRequestDTO to MPApprovalSubscriptionRequestDTO: %s", mpApprovalRequest);

        MPApprovalSubscriptionResponseDTO mpApprovalResponse = mercadoPagoClient.createSubscription(mpApprovalRequest);
        
        LOG.debugf("Received MPApprovalSubscriptionResponseDTO from MercadoPago: %s", mpApprovalResponse);
        return mpMapper.toProviderSubscriptionResponseDTO(mpApprovalResponse);
    }

    @Override
    public void handleWebhook(String payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleWebhook'");
    }
}
