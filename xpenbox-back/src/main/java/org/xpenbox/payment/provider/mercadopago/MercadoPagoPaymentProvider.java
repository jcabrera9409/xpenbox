package org.xpenbox.payment.provider.mercadopago;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.xpenbox.payment.provider.PaymentProvider;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.MercadoPagoClient;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mercadopago.client.dto.MPUpdateSubscriptionRequestDTO;
import org.xpenbox.payment.provider.mercadopago.mapper.MPMapper;

import jakarta.enterprise.context.ApplicationScoped;

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

        MPUpdateSubscriptionRequestDTO mpUpdateRequest = new MPUpdateSubscriptionRequestDTO("cancelled");
        MPApprovalSubscriptionResponseDTO mpUpdateResponse = mercadoPagoClient.updateSubscription(subscriptionId, mpUpdateRequest);
        
        LOG.debugf("Received MPApprovalSubscriptionResponseDTO from MercadoPago: %s", mpUpdateResponse);
        return mpMapper.toProviderSubscriptionResponseDTO(mpUpdateResponse);
    }

    @Override
    public void handleWebhook(String payload) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleWebhook'");
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
}
