package org.xpenbox.payment.provider.mercadopago;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.PaymentProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import org.xpenbox.payment.provider.dto.ProviderPaymentResponseDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.MercadoPagoClient;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPPaymentResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPUpdateSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.mapper.MPMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

/**
 * Implementation of the PaymentProvider interface for MercadoPago. This class provides specific logic for interacting with the MercadoPago API to manage subscriptions and handle webhooks.
 */
@ApplicationScoped
public class MercadoPagoPaymentProvider implements PaymentProvider {
    private static final Logger LOG = Logger.getLogger(MercadoPagoPaymentProvider.class);

    @ConfigProperty(name = "mp.webhook.secret-key")
    private String webhookSecretKey;

    private final MercadoPagoClient mercadoPagoClient;
    private final MPMapper mpMapper;

    public MercadoPagoPaymentProvider(
        @RestClient MercadoPagoClient mercadoPagoClient,
        MPMapper mpMapper) {
        this.mercadoPagoClient = mercadoPagoClient;
        this.mpMapper = mpMapper;
    }

    @Override
    public ProviderSubscriptionResponseDTO getSubscription(String subscriptionId) {
        LOG.infof("Retrieving subscription with ID %s using MercadoPago", subscriptionId);
        MPApprovalSubscriptionResponseDTO mpResponse = mercadoPagoClient.getSubscription(subscriptionId);
        
        LOG.debugf("Received MPApprovalSubscriptionResponseDTO from MercadoPago: %s", mpResponse);
        return mpMapper.toProviderSubscriptionResponseDTO(mpResponse);
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
    public ProviderSubscriptionResponseDTO cancelSubscription(String subscriptionId) {
        LOG.infof("Cancelling subscription with ID %s using MercadoPago", subscriptionId);

        try {
            MPUpdateSubscriptionRequestDTO mpUpdateRequest =
                    new MPUpdateSubscriptionRequestDTO("cancelled");
            MPApprovalSubscriptionResponseDTO mpUpdateResponse =
                    mercadoPagoClient.updateSubscription(subscriptionId, mpUpdateRequest);
            if (mpUpdateResponse == null) {
                throw new RuntimeException("Null response from MercadoPago");
            }

            LOG.debugf("Received MPApprovalSubscriptionResponseDTO from MercadoPago: %s",
                    mpUpdateResponse);
            return mpMapper.toProviderSubscriptionResponseDTO(mpUpdateResponse);

        } catch (WebApplicationException ex) {
            if (isAlreadyCancelledError(ex)) {
                LOG.infof("Subscription %s already cancelled in MercadoPago (idempotent success)",
                        subscriptionId);
                return new ProviderSubscriptionResponseDTO(
                    subscriptionId,
                    null,
                    "cancelled",
                    PaymentProviderType.MERCADOPAGO
                );
            }

            LOG.errorf(ex, "Error cancelling subscription %s in MercadoPago", subscriptionId);
            throw ex;
        }
    }

    @Override
    public ProviderPaymentResponseDTO getPayment(String paymentId) {
        LOG.infof("Retrieving payment with ID %s using MercadoPago", paymentId);
        MPPaymentResponseDTO paymentResponse = mercadoPagoClient.getPayment(paymentId);

        if (paymentResponse == null) {
            LOG.errorf("Failed to retrieve payment with ID %s using MercadoPago: No response received", paymentId);
            throw new RuntimeException("Failed to retrieve payment: No response from MercadoPago");
        }

        return mpMapper.toProviderPaymentResponseDTO(paymentResponse);
    }

    @Override
    public void validateWebhook(String signature, String requestId, String dataId) {
        String ts = signature.split(",")[0].split("=")[1];
        String expectedHash = signature.split(",")[1].split("=")[1];

        String payload = String.format("id:%s;request-id:%s;ts:%s;", dataId.toLowerCase(), requestId, ts);

        // Generate cypher signature using HMAC SHA256 with the webhook secret key
        String generatedHash = generateHmacSha256(payload, webhookSecretKey);
        
        if (!generatedHash.equals(expectedHash)) {
            LOG.errorf("Webhook signature validation failed. Expected: %s, Generated: %s", expectedHash, generatedHash);
            throw new SecurityException("Invalid webhook signature");
        }
        
        LOG.infof("Webhook signature validated successfully");
    }
    
    /**
     * Generates HMAC SHA256 signature for the given payload using the secret key.
     * 
     * @param payload The data to sign
     * @param secretKey The secret key for signing
     * @return The hexadecimal representation of the HMAC SHA256 signature
     */
    private String generateHmacSha256(String payload, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            // Convert bytes to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            LOG.errorf(e, "Error generating HMAC SHA256 signature");
            throw new RuntimeException("Failed to generate HMAC SHA256 signature", e);
        }
    }

    /**
     * Checks if the WebApplicationException corresponds to an "already cancelled" error from MercadoPago. This is used to handle idempotent cancellation requests gracefully, treating them as successful if the subscription is already cancelled.
     * @param ex the WebApplicationException thrown during the cancellation attempt, which may contain details about the error response from MercadoPago
     * @return true if the exception indicates that the subscription was already cancelled, allowing the cancellation to be treated as successful; false otherwise, indicating that the error is due to a different issue that should be handled as a failure
    */
    private boolean isAlreadyCancelledError(WebApplicationException ex) {
        try {
            if (ex.getResponse() == null) return false;

            int status = ex.getResponse().getStatus();
            String body = ex.getResponse().readEntity(String.class);

            return status == 400 &&
                body != null &&
                body.contains("cancelled preapproval");

        } catch (Exception e) {
            return false;
        }
    }
}
