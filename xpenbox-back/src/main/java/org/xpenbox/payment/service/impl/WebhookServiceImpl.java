package org.xpenbox.payment.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.PaymentProvider;
import org.xpenbox.payment.provider.PaymentProviderFactory;
import org.xpenbox.payment.service.IWebhookService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the IWebhookService interface for handling webhooks from payment providers. This class provides specific logic for processing incoming webhook payloads and validating webhook requests to ensure they are legitimate and have not been tampered with. The implementation can be extended to include specific handling for different payment providers, such as Stripe, PayPal, MercadoPago, or others, allowing for a standardized way to manage webhook events across various payment services.
 */
@ApplicationScoped
public class WebhookServiceImpl implements IWebhookService {
    private static final Logger LOG = Logger.getLogger(WebhookServiceImpl.class);

    private final PaymentProviderFactory paymentProviderFactory;

    public WebhookServiceImpl(PaymentProviderFactory paymentProviderFactory) {
        this.paymentProviderFactory = paymentProviderFactory;
    }

    @Override
    public void validateWebhook(PaymentProviderType providerType, String signature, String requestId, String dataId) {
        LOG.infof("Validating webhook for provider %s with requestId %s and dataId %s", providerType, requestId, dataId);
        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(providerType);
        paymentProvider.validateWebhook(signature, requestId, dataId);
    }
    
}
