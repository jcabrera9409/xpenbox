package org.xpenbox.payment.service;

import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.mercadopago.dto.MPWebhookRequestDTO;

/**
 * The IWebhookService interface defines the contract for handling webhooks from payment providers. It includes methods for processing incoming webhook payloads and validating webhook requests to ensure they are legitimate and have not been tampered with. Implementations of this interface will provide the specific logic for handling webhooks from different payment providers, such as Stripe, PayPal, MercadoPago, or others, allowing for a standardized way to manage webhook events across various payment services.
 */
public interface IWebhookService {
    
    /**
     * Handles incoming webhooks from the payment provider. This method should process the payload received from the payment provider, which may contain information about subscription events, payment updates, or other relevant notifications.
     * @param providerType the type of the payment provider, which can be used to determine the specific logic for handling the webhook
     * @param signature the signature provided in the webhook request, which should be compared against the expected signature generated using the payment provider's secret key and the request payload
     * @param requestId the unique identifier of the webhook request, which can be used to track and log the request for auditing purposes
     * @param dataId the unique identifier of the data associated with the webhook event, which can be used to correlate the webhook event with specific actions or records in the system
     */
    void validateWebhook(PaymentProviderType providerType, String signature, String requestId, String dataId);

    /**
     * Registers a new webhook for a payment provider. This method can be used to set up the necessary configurations and endpoints for receiving webhooks from the specified payment provider. The implementation may involve storing the webhook details in a database, configuring the payment provider's dashboard, or performing any other necessary setup to ensure that webhooks are received and processed correctly.
     * @param providerType the type of the payment provider for which the webhook is being registered, which can be used to determine the specific logic for setting up the webhook
     * @param webhookRequestDTO the data transfer object containing the details of the webhook request, which may include information such as the endpoint URL, authentication credentials, and any other relevant configuration parameters needed to register the webhook with the payment provider
     */
    void registerNewPaymentProviderWebhook(PaymentProviderType providerType, MPWebhookRequestDTO webhookRequestDTO);
}
