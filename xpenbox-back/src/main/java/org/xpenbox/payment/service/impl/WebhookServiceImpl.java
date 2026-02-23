package org.xpenbox.payment.service.impl;

import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.enforcement.service.IPlanSnapshotService;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.SubscriptionPayment;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;
import org.xpenbox.payment.entity.SubscriptionPayment.PaymentStatus;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.mapper.SubscriptionPaymentMapper;
import org.xpenbox.payment.provider.PaymentProvider;
import org.xpenbox.payment.provider.PaymentProviderFactory;
import org.xpenbox.payment.provider.dto.ProviderPaymentResponseDTO;
import org.xpenbox.payment.provider.mercadopago.dto.MPWebhookRequestDTO;
import org.xpenbox.payment.repository.SubscriptionPaymentRepository;
import org.xpenbox.payment.repository.SubscriptionRepository;
import org.xpenbox.payment.service.ISubscriptionService;
import org.xpenbox.payment.service.IWebhookService;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Implementation of the IWebhookService interface for handling webhooks from payment providers. This class provides specific logic for processing incoming webhook payloads and validating webhook requests to ensure they are legitimate and have not been tampered with. The implementation can be extended to include specific handling for different payment providers, such as Stripe, PayPal, MercadoPago, or others, allowing for a standardized way to manage webhook events across various payment services.
 */
@ApplicationScoped
public class WebhookServiceImpl implements IWebhookService {
    private static final Logger LOG = Logger.getLogger(WebhookServiceImpl.class);

    @ConfigProperty(name = "subscription.grace.period.days")
    private Integer subscriptionGracePeriodDays;

    private final ISubscriptionService subscriptionService;
    private final PaymentProviderFactory paymentProviderFactory;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final SubscriptionPaymentMapper subscriptionPaymentMapper;
    private final IPlanSnapshotService planSnapshotService;

    public WebhookServiceImpl(
        ISubscriptionService subscriptionService,
        PaymentProviderFactory paymentProviderFactory,
        SubscriptionRepository subscriptionRepository,
        SubscriptionPaymentRepository subscriptionPaymentRepository,
        SubscriptionPaymentMapper subscriptionPaymentMapper,
        IPlanSnapshotService planSnapshotService
    ) {
        this.subscriptionService = subscriptionService;
        this.paymentProviderFactory = paymentProviderFactory;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPaymentRepository = subscriptionPaymentRepository;
        this.subscriptionPaymentMapper = subscriptionPaymentMapper;
        this.planSnapshotService = planSnapshotService;
    }

    @Override
    public void validateWebhook(PaymentProviderType providerType, String signature, String requestId, String dataId) {
        LOG.infof("Validating webhook for provider %s with requestId %s and dataId %s", providerType, requestId, dataId);
        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(providerType);
        paymentProvider.validateWebhook(signature, requestId, dataId);
    }

    @Override
    public void registerNewPaymentProviderWebhook(PaymentProviderType providerType, MPWebhookRequestDTO webhookRequestDTO) {
        LOG.infof("Registering new webhook for provider %s with dataId %s", providerType, webhookRequestDTO.data().id());
        if (!isPaymentWebhook(webhookRequestDTO.type())) {
            LOG.warnf("Received webhook with dataId %s that is not a payment event (type: %s). Skipping processing.", webhookRequestDTO.data().id(), webhookRequestDTO.type());
            return;
        }

        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(providerType);
        ProviderPaymentResponseDTO paymentResponse = paymentProvider.getPayment(webhookRequestDTO.data().id().toString());

        if (paymentResponse == null) {
            LOG.warnf("Failed to retrieve payment details for webhook with dataId %s: No payment response received", webhookRequestDTO.data().id());
            return;
        }
        
        // Acquire a pessimistic write lock (SELECT FOR UPDATE) on the subscription row.
        // This serializes concurrent webhook calls for the same subscription:
        // only one thread proceeds at a time; the others wait and then find the payment already registered.
        Subscription subscription = getSubscriptionByIdAndProviderTypeWithLock(paymentResponse.subscriptionId(), providerType);
        if (subscription == null) {
            LOG.warnf("No subscription found for payment with subscription ID %s and provider %s. Skipping processing.", paymentResponse.subscriptionId(), providerType);
            return;
        }

        // This check is now safe: it runs AFTER the lock is held, so no two threads can pass it simultaneously.
        SubscriptionPayment existingPayment = getSubscriptionPaymentExists(subscription.id, paymentResponse.id().toString(), providerType.name());
        if (existingPayment != null) {
            LOG.warnf("SubscriptionPayment already exists for subscription ID %d, provider payment ID %s, and provider %s. Skipping processing to avoid duplicates.", subscription.id, paymentResponse.id(), providerType.name());
            return;
        }

        LOG.infof("Registering payment with ID %s for subscription ID %d", paymentResponse.id(), subscription.id);
        registerPayment(paymentResponse, subscription, providerType);

        if (paymentResponse.status() == PaymentStatus.APPROVED) {
            LOG.infof("Payment with ID %s for subscription ID %d has been approved. Updating subscription status.", paymentResponse.id(), subscription.id);
            updateSubscriptionStatus(subscription, paymentResponse);

            planSnapshotService.clearPlanSnapshotByEmail(subscription.getUser().getEmail());
            LOG.infof("Cleared plan snapshot for user with email: %s after successful payment for subscription ID %d", subscription.getUser().getEmail(), subscription.id);
        }
    }
    
    /**
     * Registers a new payment in the system by creating a SubscriptionPayment record in the database. This method takes a ProviderPaymentResponseDTO containing the details of the payment, a Subscription entity associated with the payment, and the PaymentProviderType to determine the provider. It uses the SubscriptionPaymentMapper to convert the payment response into a SubscriptionPayment entity, which is then persisted to the database using the SubscriptionPaymentRepository.
     * @param paymentResponse the ProviderPaymentResponseDTO containing the details of the payment received from the payment provider, which includes information such as the payment ID, amount, currency, status, and subscription ID that can be used to create a new SubscriptionPayment record
     * @param subscription the Subscription entity associated with the payment, which can be used to link the SubscriptionPayment record to the correct subscription in the database
     * @param providerType the type of the payment provider, which can be used to determine the specific logic for mapping the payment response to a SubscriptionPayment entity based on the provider's specifications
     */
    private void registerPayment(ProviderPaymentResponseDTO paymentResponse, Subscription subscription, PaymentProviderType providerType) {
        LOG.info("Registering new SubscriptionPayment record in the database");
        
        SubscriptionPayment subscriptionPayment = subscriptionPaymentMapper.toEntity(paymentResponse, subscription, providerType);
        subscriptionPaymentRepository.persist(subscriptionPayment);
        LOG.debugf("SubscriptionPayment record created successfully: %s", subscriptionPayment);
    }

    /**
     * Updates the subscription status based on the payment response received from the payment provider. This method takes a Subscription entity and a ProviderPaymentResponseDTO as parameters, and updates the subscription's status, end date, and next billing date based on the information provided in the payment response. The subscription is then persisted to the database using the SubscriptionRepository.
     * @param subscription the Subscription entity that needs to be updated based on the payment response
     * @param paymentResponse the ProviderPaymentResponseDTO containing the details of the payment response received from the payment provider, which includes information such as the payment status, approval date, and subscription ID that can be used to determine how to update the subscription
     */
    private void updateSubscriptionStatus(Subscription subscription, ProviderPaymentResponseDTO paymentResponse) {
        LOG.infof("Updating subscription status for subscription ID %d based on payment response with status %s", subscription.id, paymentResponse.status());

        Subscription currentSubscription = subscriptionService.findActiveSubscription(subscription.getUser().id);
        if (currentSubscription != null) {
            currentSubscription.setStatus(SubscriptionStatus.CANCELLED);
            currentSubscription.setRenew(false);
            subscriptionRepository.persist(currentSubscription);
            LOG.debugf("Existing active subscription with ID %d cancelled successfully", currentSubscription.id);
        }

        LocalDateTime nextBillingDate = paymentResponse.dateApproved().plusMonths(subscription.getPlan().getBillingCycle().getFrecuencyValue());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setRenew(true);
        subscription.setEndDate(nextBillingDate.plusDays(subscriptionGracePeriodDays));
        subscription.setNextBillingDate(nextBillingDate);

        subscriptionRepository.persist(subscription);
        LOG.debugf("Subscription with ID %d updated successfully with new status %s and next billing date %s", subscription.id, subscription.getStatus(), subscription.getNextBillingDate());
    }

    /**
     * Retrieves a subscription based on the provided subscription ID and payment provider type, acquiring a pessimistic write lock (SELECT FOR UPDATE) on the subscription record. This method queries the SubscriptionRepository to find a subscription that matches the given provider subscription ID and provider type, while also applying a lock to prevent concurrent modifications. If a matching subscription is found, it is returned; otherwise, null is returned, indicating that no subscription was found for the specified criteria.
     * @param subscriptionId the unique identifier of the subscription to retrieve, which should be provided by the payment provider when the subscription was created
     * @param providerType the type of the payment provider, which can be used to determine the specific logic for retrieving the subscription based on the provider's specifications
     * @return the Subscription entity that matches the provided subscription ID and provider type, or null if no matching subscription is found
     */
    private Subscription getSubscriptionByIdAndProviderTypeWithLock(String subscriptionId, PaymentProviderType providerType) {
        LOG.infof("Retrieving subscription with ID %s for provider %s (with exclusive lock)", subscriptionId, providerType);
        Subscription subscription = subscriptionRepository.findByProviderSubscriptionIdAndProviderWithLock(subscriptionId, providerType.name())
            .orElse(null);

        LOG.debugf("Retrieved subscription with lock: %s", subscription);
        return subscription;
    }

    /**
     * Checks for the existence of a SubscriptionPayment record based on the provided subscription ID, provider payment ID, and provider. This method queries the SubscriptionPaymentRepository to find a record that matches the given criteria. If a matching SubscriptionPayment is found, it is returned; otherwise, null is returned, indicating that no such record exists in the database.
     * @param subscriptionId the unique identifier of the subscription associated with the payment, which should be provided by the payment provider when the subscription was created
     * @param providerPaymentId the unique identifier of the payment provided by the payment provider, which should be included in the webhook payload to correlate the payment with the subscription
     * @param provider the type of the payment provider, which can be used to determine the specific logic for retrieving the SubscriptionPayment based on the provider's specifications
     * @return the SubscriptionPayment entity that matches the provided subscription ID, provider payment ID, and provider, or null if no matching record is found
     */
    private SubscriptionPayment getSubscriptionPaymentExists(Long subscriptionId, String providerPaymentId, String provider) {
        LOG.infof("Checking for existing SubscriptionPayment with subscriptionId: %d, providerPaymentId: %s, provider: %s", subscriptionId, providerPaymentId, provider);
        SubscriptionPayment subscriptionPayment = subscriptionPaymentRepository.findBySubscriptionIdAndProviderPaymentIdAndProvider(subscriptionId, providerPaymentId, provider)
            .orElse(null);
        LOG.debugf("Found SubscriptionPayment: %s", subscriptionPayment);
        return subscriptionPayment;
    }

    /**
     * Checks if the webhook event type corresponds to a payment event. This method compares the provided event type against the expected value for payment events, which can vary depending on the payment provider. If the event type matches the expected value for payment events, it returns true; otherwise, it returns false, indicating that the webhook event is not related to a payment and may be skipped or handled differently.
     * @param type the type of the webhook event, which should be compared against the expected value for payment events based on the payment provider's specifications
     * @return true if the event type corresponds to a payment event, false otherwise
     */
    private boolean isPaymentWebhook(String type) {
        LOG.infof("Checking if webhook event type %s is a payment event", type);
        return type.equalsIgnoreCase("payment");
    }
}
