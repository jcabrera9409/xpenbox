package org.xpenbox.payment.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.exception.BadRequestException;
import org.xpenbox.exception.ForbiddenException;
import org.xpenbox.payment.dto.PreApprovalSubscriptionRequestDTO;
import org.xpenbox.payment.dto.PreApprovalSubscriptionResponseDTO;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.Plan.PlanStatus;
import org.xpenbox.payment.mapper.PaymentMapper;
import org.xpenbox.payment.provider.PaymentProvider;
import org.xpenbox.payment.provider.PaymentProviderFactory;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionRequestDTO;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.payment.provider.mapper.ProviderMapper;
import org.xpenbox.payment.repository.PlanRepository;
import org.xpenbox.payment.repository.SubscriptionRepository;
import org.xpenbox.payment.service.IPaymentService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PaymentServiceImpl implements IPaymentService {
    private static final Logger LOG = Logger.getLogger(PaymentServiceImpl.class);

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentProviderFactory paymentProviderFactory;
    private final ProviderMapper providerMapper;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(
        UserRepository userRepository,
        PlanRepository planRepository,
        SubscriptionRepository subscriptionRepository,
        PaymentProviderFactory paymentProviderFactory,
        ProviderMapper providerMapper,
        PaymentMapper paymentMapper
    ) {
        this.userRepository = userRepository;
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentProviderFactory = paymentProviderFactory;
        this.providerMapper = providerMapper;
        this.paymentMapper = paymentMapper;
    }

    @Override
    public PreApprovalSubscriptionResponseDTO createPreApprovalSubscription(PreApprovalSubscriptionRequestDTO request, String userEmail) {
        LOG.infof("Creating pre-approval subscription for user %s with plan resource code %s and payment provider %s", userEmail, request.resourceCodePlan(), request.paymentProviderType());
        User user = validateAndGetUser(userEmail);
        
        Plan plan = validateAndGetPlan(request.resourceCodePlan());

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
