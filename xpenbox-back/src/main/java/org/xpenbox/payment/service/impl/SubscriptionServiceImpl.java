package org.xpenbox.payment.service.impl;

import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.exception.ForbiddenException;
import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.Plan.PlanStatus;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.mapper.SubscriptionMapper;
import org.xpenbox.payment.provider.PaymentProvider;
import org.xpenbox.payment.provider.PaymentProviderFactory;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mapper.ProviderMapper;
import org.xpenbox.payment.repository.PlanRepository;
import org.xpenbox.payment.repository.SubscriptionRepository;
import org.xpenbox.payment.service.ISubscriptionService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubscriptionServiceImpl implements ISubscriptionService {
    private static final Logger LOG = Logger.getLogger(SubscriptionServiceImpl.class);

    @ConfigProperty(name = "plan.free.resourcecode")
    private String freePlanResourceCode;

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final ProviderMapper providerMapper;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionServiceImpl(
        UserRepository userRepository,
        PlanRepository planRepository,
        SubscriptionRepository subscriptionRepository,
        PaymentProviderFactory paymentProviderFactory,
        ProviderMapper providerMapper,
        SubscriptionMapper subscriptionMapper
    ) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentProviderFactory = paymentProviderFactory;
        this.providerMapper = providerMapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public SubscriptionResponseDTO getActiveSubscription(String userEmail) {
        LOG.infof("Retrieving active subscription for user with email: %s", userEmail);
        User user = validateAndGetUser(userEmail);
        Subscription activeSubscription = findActiveSubscription(user.id);

        if (activeSubscription == null) {
            LOG.infof("No active subscription found for user with email: %s, returning free subscription", userEmail);
            return subscriptionMapper.toDTO(createFreeSubscription(user));
        }

        return subscriptionMapper.toDTO(activeSubscription);
    }

    @Override
    public PreApprovalSubscriptionResponseDTO createPreApprovalSubscription(PreApprovalSubscriptionRequestDTO request, String userEmail) {
        LOG.infof("Creating pre-approval subscription for user %s with plan resource code %s and payment provider %s", userEmail, request.resourceCodePlan(), request.paymentProviderType());
        User user = validateAndGetUser(userEmail);
        Plan plan = validateAndGetPlan(request.resourceCodePlan());

        Subscription activeSubscription = findActiveSubscription(user.id);

        if (activeSubscription != null && activeSubscription.getPlan().id == plan.id) {
            LOG.warnf("User %s already has an active subscription for plan %s", userEmail, request.resourceCodePlan());
            throw new BadRequestException("User already has an active subscription for this plan");
        }

        LOG.infof("Checking for existing pending subscription for user %s", userEmail);
        Subscription existingPendingSubscription = findPendingSubscription(user.id);

        if (existingPendingSubscription == null) {
            LOG.infof("No existing pending subscription found for user %s, creating new pre-approval subscription", userEmail);
            existingPendingSubscription = createProSubscription(user, plan, request.paymentProviderType());
        } else if (isPendingSubscriptionExpiredOrDifferentProvider(existingPendingSubscription, request.paymentProviderType())) {
            LOG.infof("Existing pending subscription for user %s is either expired or has a different provider, cancelling it before creating a new one", userEmail);
            cancelExistingSubscription(existingPendingSubscription);
            existingPendingSubscription = createProSubscription(user, plan, request.paymentProviderType());
        }
        
        LOG.infof("Returning pre-approval subscription for user %s with plan %s and payment provider %s", userEmail, request.resourceCodePlan(), request.paymentProviderType());
        return new PreApprovalSubscriptionResponseDTO(
            existingPendingSubscription.getProviderSubscriptionUrl()
        );
        
    }

    @Override
    public void cancelActiveSubscription(String userEmail) {
        LOG.infof("Cancelling active subscription for user with email: %s", userEmail);
        User user = validateAndGetUser(userEmail);
        Subscription activeSubscription = findActiveSubscription(user.id);

        if (activeSubscription == null) {
            LOG.warnf("No active subscription found for user with email: %s", userEmail);
            throw new BadRequestException("No active subscription found to cancel");
        }

        if (!isProSubscription(activeSubscription)) {
            LOG.warnf("Active subscription with resource code %s for user ID %s is a free subscription and cannot be cancelled", activeSubscription.getResourceCode(), user.id);
            throw new BadRequestException("Active subscription is a free subscription and cannot be cancelled");
        }
        
        cancelExistingSubscription(activeSubscription);
    }

    /**
     * Creates a free subscription for the given user. This method is called when a user does not have an active subscription, and it assigns them to the free plan. The free plan is identified by a specific resource code defined in the configuration. If the free plan is not found, a BadRequestException is thrown.
     * @param user The user for whom the free subscription will be created.
     * @return The created Subscription entity representing the free subscription for the user.
     */
    private Subscription createFreeSubscription(User user) {
        LOG.infof("Creating free subscription for user ID %s", user.id);
        Plan freePlan = planRepository.findByResourceCode(freePlanResourceCode)
            .orElseThrow(() -> {
                LOG.warnf("Free plan not found");
                return new BadRequestException("Free plan not found");
            });

        Subscription subscriptionEntity = subscriptionMapper.createFreeSubscriptionEntity(user, freePlan);
        subscriptionRepository.persist(subscriptionEntity);

        LOG.infof("Free subscription created successfully for user ID %s with plan resource code %s", user.id, freePlanResourceCode);
        return subscriptionEntity;
    }

    /**
     * Creates a pro subscription for the given user, plan, and payment provider type. This method interacts with the specified payment provider to create a pre-approval subscription based on the provided plan and user information. It then persists the created subscription entity in the subscription repository. If the pre-approval subscription creation fails, a BadRequestException is thrown.
     * @param user The user for whom the pro subscription will be created.
     * @param plan The plan for which the pro subscription will be created.
     * @param providerType The type of payment provider to use for creating the pre-approval subscription.
     * @return The created Subscription entity representing the pro subscription for the user, or null if the subscription creation fails.
     */
    private Subscription createProSubscription(User user, Plan plan, PaymentProviderType providerType) {
        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(providerType);
        ProviderSubscriptionRequestDTO subscriptionRequest = providerMapper.toSubscriptionPlanRequestDTO(plan, user, providerType);
        
        ProviderSubscriptionResponseDTO subscriptionResponse = paymentProvider.createPreApprovalSubscription(subscriptionRequest);
  
        if (subscriptionResponse == null) {
            LOG.warnf("Failed to create pre-approval subscription for user %s and plan %s", user.getEmail(), plan.getResourceCode());
            throw new BadRequestException("Failed to create pre-approval subscription");
        }
        
        LOG.infof("Pre-approval subscription created successfully for user %s and plan %s with provider %s", user.getEmail(), plan.getResourceCode(), providerType);
        Subscription subscriptionEntity = subscriptionMapper.toEntity(subscriptionResponse, plan, user);
        subscriptionRepository.persist(subscriptionEntity);

        LOG.infof("Subscription entity persisted successfully for user %s and plan %s with provider %s", user.getEmail(), plan.getResourceCode(), providerType);
        return subscriptionEntity;
    }

    /**
     * Cancels an existing subscription. This method interacts with the payment provider associated with the subscription to cancel it. Depending on the current status of the subscription (PENDING or ACTIVE), it updates the subscription's status and next billing date accordingly. If the subscription has a non-cancellable status, a BadRequestException is thrown. After updating the subscription, it is persisted in the subscription repository.
     * @param subscription The Subscription entity representing the subscription to be cancelled.
     */
    private void cancelExistingSubscription(Subscription subscription) {
        LOG.infof("Cancelling existing subscription with resource code %s for user ID %s", subscription.getResourceCode(), subscription.getUser().id);
        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(subscription.getProvider());
        paymentProvider.cancelSubscription(subscription.getProviderSubscriptionId());
        
        if (subscription.getStatus().equals(SubscriptionStatus.PENDING)) {
            LOG.infof("Existing subscription with resource code %s for user ID %s is pending, setting status to cancelled", subscription.getResourceCode(), subscription.getUser().id);
            subscription.setStatus(SubscriptionStatus.CANCELLED);
        } else if (subscription.getStatus().equals(SubscriptionStatus.ACTIVE)) {
            LOG.infof("Existing subscription with resource code %s for user ID %s is active, setting status to cancelled", subscription.getResourceCode(), subscription.getUser().id);
            subscription.setNextBillingDate(subscription.getEndDate());
        } else {
            LOG.warnf("Existing subscription with resource code %s for user ID %s has non-cancellable status %s", subscription.getResourceCode(), subscription.getUser().id, subscription.getStatus());
            throw new BadRequestException("Existing subscription has non-cancellable status");
        }

        subscriptionRepository.persist(subscription);
        LOG.infof("Existing subscription with resource code %s for user ID %s cancelled successfully", subscription.getResourceCode(), subscription.getUser().id);
    }

    @Override
    public Subscription findActiveSubscription(Long userId) {
        LOG.infof("Checking for existing active subscription for user ID %s", userId);
        return findSubscriptionByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    /**
     * Finds a pending subscription for the given user ID. This method queries the subscription repository to find a subscription with the status of PENDING for the specified user. If a pending subscription is found, it is returned; otherwise, null is returned.
     * @param userId The ID of the user for whom to find the pending subscription.
     * @return The Subscription entity representing the pending subscription for the user, or null if no pending subscription is found.
     */
    private Subscription findPendingSubscription(Long userId) {
        LOG.infof("Checking for existing pending subscription for user ID %s", userId);
        return findSubscriptionByUserIdAndStatus(userId, SubscriptionStatus.PENDING);
    }

    /**
     * Finds a subscription for the given user ID and subscription status. This method queries the subscription repository to find a subscription that matches the specified user ID and subscription status. If a matching subscription is found, it is returned; otherwise, null is returned.
     * @param userId The ID of the user for whom to find the subscription.
     * @param status The status of the subscription to find (e.g., ACTIVE, PENDING).
     * @return The Subscription entity representing the subscription for the user with the specified status, or null if no matching subscription is found.
     */
    private Subscription findSubscriptionByUserIdAndStatus(Long userId, SubscriptionStatus status) {
        LOG.infof("Finding subscription for user ID %s with status %s", userId, status);
        return subscriptionRepository.findByUserIdAndStatus(userId, status)
            .orElse(null);
    }
    
    /**
     * Verifies if a pending subscription is either expired or has a different provider than the specified provider type. This method checks if the start date of the pending subscription is more than 1 hour in the past (indicating that it is expired) or if the provider of the subscription does not match the specified provider type. If either condition is true, it returns true, indicating that the pending subscription is either expired or has a different provider; otherwise, it returns false.
     * @param subscription The Subscription entity to check for expiration or provider mismatch.
     * @param providerType The type of payment provider to compare against the subscription's provider.
     * @return true if the pending subscription is either expired or has a different provider than the specified provider type, false otherwise.
     */
    private boolean isPendingSubscriptionExpiredOrDifferentProvider(Subscription subscription, PaymentProviderType providerType) {
        return subscription.getStartDate().plusHours(1).isBefore(LocalDateTime.now()) 
                || !subscription.getProvider().equals(providerType.name());
    }

    /**
     * Checks if a subscription is a pro subscription by verifying if its associated plan's resource code is different from the free plan's resource code. This method compares the resource code of the subscription's plan with the configured free plan resource code. If they are different, it returns true, indicating that the subscription is a pro subscription; otherwise, it returns false, indicating that it is a free subscription.
     * @param subscription The Subscription entity to check if it is a pro subscription.
     * @return true if the subscription is a pro subscription (i.e., its plan's resource code is different from the free plan's resource code), false if it is a free subscription.
     */
    private boolean isProSubscription(Subscription subscription) {
        LOG.infof("Checking if subscription with resource code %s is a pro subscription", subscription.getResourceCode());
        if (subscription.getPlan().getResourceCode().equalsIgnoreCase(freePlanResourceCode)) {
            LOG.infof("Subscription with resource code %s is a free subscription", subscription.getResourceCode());
            return false;
        }

        return true;
    }

    /**
     * Validates the existence and active status of a plan based on the provided resource code. This method queries the plan repository to find a plan with the specified resource code. If no plan is found, it throws a BadRequestException indicating that the plan was not found. If a plan is found but its status is not ACTIVE, it throws a BadRequestException indicating that the plan is not active. If a valid active plan is found, it is returned.
     * @param resourceCode The resource code of the plan to validate and retrieve.
     * @return The Plan entity representing the validated active plan with the specified resource code.
     */
    private Plan validateAndGetPlan(String resourceCode) {
        LOG.infof("Validating plan with resource code: %s", resourceCode);
        
        Plan plan = planRepository.findByResourceCode(resourceCode)
            .orElseThrow(() -> {
                LOG.warnf("Plan with resource code %s not found", resourceCode);
                return new BadRequestException("Plan not found");
            });

        if (!plan.getStatus().equals(PlanStatus.ACTIVE)) {
            LOG.warnf("Plan with resource code %s is not active", resourceCode);
            throw new BadRequestException("Plan is not active");
        }

        return plan;
    }

    /**
     * Validates the existence and active status of a user based on the provided email. This method queries the user repository to find a user with the specified email. If no user is found, it throws a BadRequestException indicating that the user was not found. If a user is found but their email is not verified, it throws a ForbiddenException indicating that the email is not verified. If a user is found but their account state is inactive, it throws a ForbiddenException indicating that the user account is inactive. If a valid active user with a verified email is found, it is returned.
     * @param email The email of the user to validate and retrieve.
     * @return The User entity representing the validated active user with the specified email.
     */
    private User validateAndGetUser(String email) {
        LOG.infof("Validating user with email: %s", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                LOG.warnf("User with email %s not found", email);
                return new BadRequestException("User not found");
            });

        if (!user.getVerified()) {
            LOG.warnf("User with email %s has not verified their email", email);
            throw new ForbiddenException("Email not verified");
        }

        if (!user.getState()) {
            LOG.warnf("User with email %s is inactive", email);
            throw new ForbiddenException("User account is inactive");
        }

        return user;
    }
}
