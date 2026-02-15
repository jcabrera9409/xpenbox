package org.xpenbox.payment.mapper;

import java.time.LocalDateTime;

import org.jboss.logging.Logger;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

@Singleton
public class PaymentMapper {
    private static final Logger LOG = Logger.getLogger(PaymentMapper.class);

    public Subscription toSubscriptionEntity(ProviderSubscriptionResponseDTO providerPlan, Plan plan, User user) {
        LOG.infof("Mapping ProviderPlanResponseDTO to Subscription entity for plan %s and user %s", plan.getResourceCode(), user.getEmail());

        Subscription subscription = new Subscription();
        subscription.setResourceCode(ResourceCode.generateSubscriptionResourceCode());
        subscription.setPlan(plan);
        subscription.setUser(user);
        subscription.setPlanPrice(plan.getPrice());
        subscription.setPlanCurrency(plan.getCurrency());
        subscription.setStartDate(LocalDateTime.now());        
        subscription.setEndDate(null);
        subscription.setNextBillingDate(null);
        subscription.setProvider(providerPlan.paymentProviderType().name());
        subscription.setProviderPlanId(providerPlan.providerSubscriptionPlanId());
        subscription.setProviderSubscriptionId(providerPlan.providerSubscriptionPlanId());
        subscription.setStatus(SubscriptionStatus.PENDING);

        return subscription;
    }
}
