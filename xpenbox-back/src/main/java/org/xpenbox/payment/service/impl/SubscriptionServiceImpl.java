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
import org.xpenbox.payment.mapper.PaymentMapper;
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
    private final PaymentMapper paymentMapper;
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionServiceImpl(
        UserRepository userRepository,
        PlanRepository planRepository,
        SubscriptionRepository subscriptionRepository,
        PaymentProviderFactory paymentProviderFactory,
        ProviderMapper providerMapper,
        PaymentMapper paymentMapper,
        SubscriptionMapper subscriptionMapper
    ) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentProviderFactory = paymentProviderFactory;
        this.providerMapper = providerMapper;
        this.paymentMapper = paymentMapper;
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public SubscriptionResponseDTO createFreeSubscription(String userEmail) {
        LOG.infof("Creating free subscription for user with email: %s", userEmail);
        User user = validateAndGetUser(userEmail);
        Subscription activeSubscription = findActiveSubscription(user.id);

        if (activeSubscription != null) {
            LOG.warnf("User with email %s already has an active subscription with resource code %s and provider %s", userEmail, activeSubscription.getResourceCode(), activeSubscription.getProvider());
            return subscriptionMapper.toDTO(activeSubscription);
        }

        Plan freePlan = planRepository.findByResourceCode(freePlanResourceCode)
            .orElseThrow(() -> {
                LOG.warnf("Free plan not found");
                return new BadRequestException("Free plan not found");
            });

        Subscription subscriptionEntity = subscriptionMapper.createFreeSubscriptionEntity(user, freePlan);

        subscriptionRepository.persist(subscriptionEntity);

        LOG.infof("Free subscription created successfully for user with email: %s", userEmail);
        return subscriptionMapper.toDTO(subscriptionEntity);
    }

    @Override
    public SubscriptionResponseDTO getActiveSubscription(String userEmail) {
        LOG.infof("Retrieving active subscription for user with email: %s", userEmail);
        User user = validateAndGetUser(userEmail);
        Subscription activeSubscription = findActiveSubscription(user.id);

        if (activeSubscription == null) {
            LOG.infof("No active subscription found for user with email: %s, returning free subscription", userEmail);
            return createFreeSubscription(userEmail);
        }

        return subscriptionMapper.toDTO(activeSubscription);
    }

    @Override
    public PreApprovalSubscriptionResponseDTO createPreApprovalSubscription(PreApprovalSubscriptionRequestDTO request, String userEmail) {
        LOG.infof("Creating pre-approval subscription for user %s with plan resource code %s and payment provider %s", userEmail, request.resourceCodePlan(), request.paymentProviderType());
        User user = validateAndGetUser(userEmail);
        Plan plan = validateAndGetPlan(request.resourceCodePlan());
        Subscription existingSubscription = findPendingSubscription(user.id);

        if (existingSubscription != null) {
            //validate if subscription expired or provider is different
            if (!existingSubscription.getProvider().equals(request.paymentProviderType().name()) || existingSubscription.getStartDate().plusHours(1).isBefore(LocalDateTime.now())) {
                cancelSubscription(existingSubscription.getResourceCode(), userEmail);
            } else if (!existingSubscription.getStartDate().plusHours(1).isBefore(LocalDateTime.now())) {
                LOG.warnf("User %s already has a pending subscription with resource code %s and provider %s", userEmail, existingSubscription.getResourceCode(), existingSubscription.getProvider());
                return new PreApprovalSubscriptionResponseDTO(
                    existingSubscription.getProviderSubscriptionUrl()
                );
            }
        }

        ProviderSubscriptionRequestDTO subscriptionRequest = providerMapper.toSubscriptionPlanRequestDTO(plan, user, request.paymentProviderType());
        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(request.paymentProviderType());
        ProviderSubscriptionResponseDTO subscriptionResponse = paymentProvider.createPreApprovalSubscription(subscriptionRequest);
  
        if (subscriptionResponse == null) {
            LOG.warnf("Failed to create pre-approval subscription for user %s and plan %s", userEmail, request.resourceCodePlan());
            throw new BadRequestException("Failed to create pre-approval subscription");
        }
        
        LOG.infof("Pre-approval subscription created successfully for user %s and plan %s with provider %s", userEmail, request.resourceCodePlan(), request.paymentProviderType());
        Subscription subscriptionEntity = paymentMapper.toSubscriptionEntity(subscriptionResponse, plan, user);
        subscriptionRepository.persist(subscriptionEntity);

        LOG.infof("Subscription entity persisted successfully for user %s and plan %s with provider %s", userEmail, request.resourceCodePlan(), request.paymentProviderType());
        return new PreApprovalSubscriptionResponseDTO(
            subscriptionResponse.checkoutUrl()
        );
    }

    @Override
    public void cancelSubscription(String resourceCode, String userEmail) {
        LOG.infof("Cancelling subscription with resource code %s for user %s", resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);
        Subscription subscription = validateAndGetSubscription(resourceCode, user.id);

        if (subscription.getUser().id != user.id) {
            LOG.warnf("Subscription with resource code %s does not belong to user %s", resourceCode, userEmail);
            throw new ForbiddenException("Subscription does not belong to the user");
        }

        if (subscription.getStatus() == Subscription.SubscriptionStatus.CANCELLED) {
            LOG.warnf("Subscription with resource code %s is already cancelled for user %s", resourceCode, userEmail);
            throw new BadRequestException("Subscription is already cancelled");
        }

        PaymentProvider paymentProvider = paymentProviderFactory.getPaymentProvider(subscription.getProvider());
        ProviderSubscriptionResponseDTO cancelResponse = paymentProvider.cancelSubscription(subscription.getResourceCode());

        if (cancelResponse == null) {
            LOG.warnf("Failed to cancel subscription with resource code %s for user %s", resourceCode, userEmail);
            throw new BadRequestException("Failed to cancel subscription");
        }

        LOG.infof("Subscription with resource code %s cancelled successfully for user %s", resourceCode, userEmail);
        subscription.setProviderSubscriptionUrl("");
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscriptionRepository.persist(subscription);
    }

    private Subscription findActiveSubscription(Long userId) {
        LOG.infof("Checking for existing active subscription for user ID %s", userId);
        return findSubscriptionByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    private Subscription findPendingSubscription(Long userId) {
        LOG.infof("Checking for existing pending subscription for user ID %s", userId);
        return findSubscriptionByUserIdAndStatus(userId, SubscriptionStatus.PENDING);
    }

    private Subscription findSubscriptionByUserIdAndStatus(Long userId, SubscriptionStatus status) {
        LOG.infof("Finding subscription for user ID %s with status %s", userId, status);
        return subscriptionRepository.findByUserIdAndStatus(userId, status)
            .orElse(null);
    }

    private Subscription validateAndGetSubscription(String resourceCode, Long userId) {
        LOG.infof("Validating subscription with resource code %s for user ID %s", resourceCode, userId);
        return subscriptionRepository.findByResourceCodeAndUserId(resourceCode, userId)
            .orElseThrow(() -> {
                LOG.warnf("Subscription with resource code %s not found for user ID %s", resourceCode, userId);
                return new BadRequestException("Subscription not found");
            });
    }

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
