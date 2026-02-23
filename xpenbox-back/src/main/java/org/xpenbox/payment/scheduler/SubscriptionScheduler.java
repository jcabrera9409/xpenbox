package org.xpenbox.payment.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.common.DateFunctions;
import org.xpenbox.enforcement.service.IPlanSnapshotService;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.SubscriptionPayment;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;
import org.xpenbox.payment.entity.SubscriptionPayment.PaymentStatus;
import org.xpenbox.payment.repository.PlanRepository;
import org.xpenbox.payment.repository.SubscriptionPaymentRepository;
import org.xpenbox.payment.repository.SubscriptionRepository;

import io.quarkus.scheduler.Scheduled;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

/**
 * Scheduler class for managing subscription-related tasks. This class is responsible for scheduling and executing tasks related to subscriptions, such as cleaning up expired subscriptions. It uses the Quarkus Scheduler to run tasks at specified intervals, allowing for automated management of subscription statuses based on their expiration dates.
 */
@Singleton
public class SubscriptionScheduler {
    private static final Logger LOG = Logger.getLogger(SubscriptionScheduler.class.getName());

    @ConfigProperty(name = "plan.pro.resourcecode")
    private String planProResourceCode;

    @ConfigProperty(name = "subscription.grace.period.days")
    private Integer subscriptionGracePeriodDays;

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final IPlanSnapshotService planSnapshotService; 

    public SubscriptionScheduler(
        PlanRepository planRepository,
        SubscriptionRepository subscriptionRepository,
        SubscriptionPaymentRepository subscriptionPaymentRepository,
        IPlanSnapshotService planSnapshotService
    ) {
        LOG.info("SubscriptionScheduler initialized");
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPaymentRepository = subscriptionPaymentRepository;
        this.planSnapshotService = planSnapshotService;
    }
    
    /**
     * Schedules a task to clean up expired subscriptions based on a cron expression defined in the application properties. This method is executed at the intervals specified by the cron expression, allowing for regular maintenance of subscription records by checking for expired subscriptions and updating their statuses accordingly. The task retrieves active subscriptions for the Pro plan and validates each subscription to determine if it has expired and whether it should be canceled or renewed based on its payment status.
     */
    @Scheduled(cron = "{scheduler.subscriptions.cleanup.cron}")
    @Transactional
    void scheduleCleanupSubscriptionTask() {
        LOG.info("Running scheduled subscription cleanup task");
        Plan proPlan = planRepository.findByResourceCode(planProResourceCode)
            .orElseThrow(() -> new IllegalStateException("Pro plan not found with resource code: " + planProResourceCode));
        
        List<Subscription> subscriptionActiveProPlan = subscriptionRepository.findAllSubscriptionsByStatusAndPlanIdAndBeforeEndDate(
            SubscriptionStatus.ACTIVE, proPlan.id, DateFunctions.currentLocalDateTime());
        
        LOG.infof("Found %d active subscriptions for Pro plan", subscriptionActiveProPlan.size());
        for (Subscription subscription : subscriptionActiveProPlan) {
            validateAndCancelSubscription(subscription);
        }
    }

    /**
     * Validates a subscription and cancels it if it has expired. This method checks if the subscription has expired by comparing its end date with the current date and time. If the subscription is set to renew, it also validates if there is a valid payment for the subscription in the current period. If a valid payment is found, the subscription's next billing date and end date are updated accordingly. If no valid payment is found or if the subscription is not set to renew, the subscription status is updated to canceled.
     * @param subscription the Subscription entity to validate and potentially cancel, which should contain the necessary information such as the end date, renew status, and provider details to perform the validation and cancellation logic based on the subscription's expiration and payment status
     */
    void validateAndCancelSubscription(Subscription subscription) {
        if (!isSubscriptionExpired(subscription)) return;
            
        if (subscription.getRenew()) {
            LOG.infof("Subscription with ID %d has expired but is set to renew. Validating payment...", subscription.id);
            SubscriptionPayment payment = findValidSubscriptionPayment(subscription);
            if (payment != null) {
                LOG.infof("Valid payment found for subscription with ID %d. Keeping subscription active.", subscription.id);
                LocalDateTime nextBillingDate = payment.getPaymentDate().plusMonths(subscription.getPlan().getBillingCycle().getFrecuencyValue());
                subscription.setNextBillingDate(nextBillingDate);
                subscription.setEndDate(nextBillingDate.plusDays(subscriptionGracePeriodDays));
                subscriptionRepository.persist(subscription);
                planSnapshotService.clearPlanSnapshotByEmail(subscription.getUser().getEmail());
                LOG.infof("Cleared plan snapshot for user with email: %s after successful payment for subscription ID %d", subscription.getUser().getEmail(), subscription.id);
                return;
            }
        }

        LOG.infof("Subscription with ID %d has expired and will be cancelled.", subscription.id);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setRenew(false);
        subscriptionRepository.persist(subscription);

        planSnapshotService.clearPlanSnapshotByEmail(subscription.getUser().getEmail());
        LOG.infof("Cleared plan snapshot for user with email: %s after subscription cancellation for subscription ID %d", subscription.getUser().getEmail(), subscription.id);
    }

    /**
     * Checks if a subscription has expired by comparing its end date with the current date and time. If the end date of the subscription is before the current date and time, it is considered expired. This method is used in the scheduled cleanup task to determine which subscriptions need to have their status updated to canceled due to expiration.
     * @param subscription the Subscription entity to check for expiration, which should contain the end date that indicates when the subscription is set to expire
     * @return true if the subscription has expired (i.e., its end date is before the current date and time), or false if it is still active (i.e., its end date is in the future)
     */
    boolean isSubscriptionExpired(Subscription subscription) {
        return subscription.getEndDate().isBefore(DateFunctions.currentLocalDateTime());
    }

    /**
     * Finds a valid subscription payment for the given subscription. This method queries the database for a payment that matches the subscription ID, provider, and payment date, and has a status of APPROVED. It is used to determine if a subscription has a valid payment in the current period.
     * @param subscription the Subscription entity to validate, which should contain the necessary information to check for valid payments, such as the provider subscription ID and provider type that can be used to query the payment records in the database
     * @return a SubscriptionPayment entity if a valid payment is found, or null if no valid payment is found for the subscription in the current period
     */
    SubscriptionPayment findValidSubscriptionPayment(Subscription subscription) {
        LOG.infof("Validating payment for subscription with resource code: %s", subscription.getResourceCode());
        LocalDateTime from = subscription.getNextBillingDate().minusMonths(subscription.getPlan().getBillingCycle().getFrecuencyValue());
        LocalDateTime to = subscription.getNextBillingDate().plusDays(subscriptionGracePeriodDays);
        SubscriptionPayment payment = subscriptionPaymentRepository.findBySubscriptionIdAndProviderAndPaymentDateInPeriodAndStatus(
            subscription.id, 
            subscription.getProvider(),
            from,
            to,
            PaymentStatus.APPROVED
        ).orElse(null);
        
        return payment;
    }
}
