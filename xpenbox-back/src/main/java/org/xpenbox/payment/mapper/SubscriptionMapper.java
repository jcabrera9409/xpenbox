package org.xpenbox.payment.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.ResourceCode;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;
import org.xpenbox.payment.entity.Plan;
import org.xpenbox.payment.entity.Subscription;
import org.xpenbox.payment.entity.Subscription.SubscriptionStatus;
import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.dto.ProviderSubscriptionResponseDTO;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between Subscription entity and SubscriptionResponseDTO. This class implements the GenericMapper interface to provide methods for mapping between the entity and DTO.
 */
@Singleton
public class SubscriptionMapper implements GenericMapper<Subscription, SubscriptionResponseDTO, SubscriptionResponseDTO, SubscriptionResponseDTO> {
    private static final Logger LOG = Logger.getLogger(SubscriptionMapper.class.getName());

    private final PlanMapper planMapper;

    public SubscriptionMapper(PlanMapper planMapper) {
        this.planMapper = planMapper;
    }

    /**
     * Converts a Subscription entity to a SubscriptionResponseDTO. This method maps all relevant fields from the Subscription entity to the DTO, including nested Plan and User details.
     * 
     * @param entity the Subscription entity to convert
     * @return the corresponding SubscriptionResponseDTO
     */
    @Override
    public SubscriptionResponseDTO toDTO(Subscription entity) {
        LOG.infof("Mapping Subscription entity to DTO: %s", entity);
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO(
            entity.getResourceCode(),
            entity.getPlanPrice(),
            entity.getPlanCurrency(),
            entity.getStartDate() != null ? entity.getStartDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null,
            entity.getEndDate() != null ? entity.getEndDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null,
            entity.getNextBillingDate() != null ? entity.getNextBillingDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null,
            entity.getRenew(),
            entity.getProvider(),
            entity.getProviderPlanId(),
            entity.getProviderSubscriptionId(),
            entity.getStatus(),
            entity.getPlan() != null ? planMapper.toDTO(entity.getPlan()) : null,
            null
        );
        return dto;
    }

    /**
     * Converts a Subscription entity to a simple SubscriptionResponseDTO. This method maps only the basic fields from the Subscription entity to the DTO, excluding nested Plan and User details for simplicity.
     * 
     * @param entity the Subscription entity to convert
     * @return the corresponding simple SubscriptionResponseDTO
     */
    @Override
    public SubscriptionResponseDTO toSimpleDTO(Subscription entity) {
        LOG.infof("Mapping Subscription entity to simple DTO: %s", entity);

        SubscriptionResponseDTO dto = new SubscriptionResponseDTO(
            entity.getResourceCode(),
            entity.getPlanPrice(),
            entity.getPlanCurrency(),
            entity.getStartDate() != null ? entity.getStartDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null,
            entity.getEndDate() != null ? entity.getEndDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null,
            entity.getNextBillingDate() != null ? entity.getNextBillingDate().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null,
            entity.getRenew(),
            entity.getProvider(),
            entity.getProviderPlanId(),
            entity.getProviderSubscriptionId(),
            entity.getStatus(),
            null, // Exclude plan details for simple DTO
            null  // Exclude user details for simple DTO
        );

        return dto;
    }

    /**
     * Converts a list of Subscription entities to a list of SubscriptionResponseDTOs. This method uses the toDTO method to convert each Subscription entity in the list to its corresponding DTO.
     * 
     * @param entities the list of Subscription entities to convert
     * @return the corresponding list of SubscriptionResponseDTOs
     */
    @Override
    public List<SubscriptionResponseDTO> toDTOList(List<Subscription> entities) {
        return entities.stream().map(this::toDTO).toList();
    }

    /**
     * Converts a SubscriptionResponseDTO to a Subscription entity. This method is used for creating new Subscription entities from DTOs. It maps the fields from the DTO to the Subscription entity, but does not handle nested Plan and User details as they are not included in the DTO for creation.
     * 
     * @param createDto the SubscriptionResponseDTO to convert
     * @param user the User associated with the Subscription (not used in this case, but included for consistency with the GenericMapper interface)
     * @return the corresponding Subscription entity
     */
    @Override
    public Subscription toEntity(SubscriptionResponseDTO createDto, User user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toEntity'");
    }
    
    /**
     * Updates an existing Subscription entity with data from a SubscriptionResponseDTO. This method is used for updating existing Subscription entities based on the data provided in the DTO. It maps the fields from the DTO to the Subscription entity, but does not handle nested Plan and User details as they are not included in the DTO for updates.
     * 
     * @param updateDto the SubscriptionResponseDTO containing the updated data
     * @param entity the existing Subscription entity to update
     * @return true if the entity was updated, false otherwise
     */
    @Override
    public boolean updateEntity(SubscriptionResponseDTO updateDto, Subscription entity) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateEntity'");
    }

    /**
     * Creates a new Subscription entity for a free subscription based on the provided User and Plan. This method generates a new Subscription entity with the appropriate fields set for a free subscription, including generating a unique resource code, setting the provider to "FREE", and associating it with the provided User and Plan.
     * @param user the User for whom the free subscription is being created, which may be used for associating the subscription with the user in the subscription provider's system
     * @param freePlan the Plan representing the free subscription plan, which may be used for setting the plan details in the subscription provider's system
     * @return a Subscription entity representing the created free subscription, with all relevant fields set for a free subscription
     */
    public Subscription createFreeSubscriptionEntity(User user, Plan freePlan) {
        Subscription subscriptionEntity = new Subscription();
        subscriptionEntity.setResourceCode(ResourceCode.generateSubscriptionPaymentResourceCode());
        subscriptionEntity.setPlan(freePlan);
        subscriptionEntity.setUser(user);
        subscriptionEntity.setPlanPrice(freePlan.getPrice());
        subscriptionEntity.setPlanCurrency(freePlan.getCurrency());
        subscriptionEntity.setStartDate(LocalDate.now().atStartOfDay());
        subscriptionEntity.setProvider(PaymentProviderType.FREE.name());
        subscriptionEntity.setProviderPlanId(subscriptionEntity.getResourceCode());
        subscriptionEntity.setProviderSubscriptionId(subscriptionEntity.getResourceCode());
        subscriptionEntity.setProviderSubscriptionUrl("");
        subscriptionEntity.setStatus(SubscriptionStatus.ACTIVE);
        
        return subscriptionEntity;
    }

    /**
     * Converts a ProviderSubscriptionResponseDTO to a Subscription entity. This method maps the fields from the ProviderSubscriptionResponseDTO to a new Subscription entity, including setting the provider details and associating it with the provided Plan and User. The resulting Subscription entity can then be persisted in the database or used for further processing.
     * @param providerPlan the ProviderSubscriptionResponseDTO containing the subscription details from the payment provider, which may include information such as the provider subscription ID, checkout URL, and subscription status
     * @param plan the Plan associated with the subscription, which may be used for setting the plan details in the Subscription entity
     * @param user the User associated with the subscription, which may be used for associating the subscription with the user in the Subscription entity
     * @return a Subscription entity representing the subscription details from the payment provider, with all relevant fields set based on the data from the ProviderSubscriptionResponseDTO, and associated with the provided Plan and User
     */
    public Subscription toEntity(ProviderSubscriptionResponseDTO providerPlan, Plan plan, User user) {
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
        subscription.setProviderSubscriptionUrl(providerPlan.checkoutUrl());
        subscription.setStatus(SubscriptionStatus.PENDING);

        return subscription;
    }
    
}
