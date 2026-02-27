package org.xpenbox.enforcement.service.impl;

import org.jboss.logging.Logger;
import org.xpenbox.enforcement.dto.SnapshotPlanDTO;
import org.xpenbox.enforcement.service.IPlanSnapshotService;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.payment.dto.SubscriptionResponseDTO;
import org.xpenbox.payment.service.ISubscriptionService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * PlanSnapshotServiceImpl is a concrete implementation of the IPlanSnapshotService interface. It provides methods to manage plan snapshots for users, including retrieving and clearing plan snapshots based on user email.
 */
@ApplicationScoped
public class PlanSnapshotServiceImpl implements IPlanSnapshotService {
    private static final Logger LOG = Logger.getLogger(PlanSnapshotServiceImpl.class);
    
    private final ISubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public PlanSnapshotServiceImpl(
        ISubscriptionService subscriptionService,
        UserRepository userRepository
    ) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
    }

    @Override
    @CacheResult(cacheName = "user-plan-snapshot")
    public SnapshotPlanDTO getPlanSnapshotByEmail(String email) {
        LOG.infof("Retrieving plan snapshot for email: %s", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        SubscriptionResponseDTO subscriptionResponseDTO = subscriptionService.getActiveSubscription(email);

        return new SnapshotPlanDTO(
            user.id,
            subscriptionResponseDTO.plan()
        );
    }

    @Override
    @CacheInvalidate(cacheName = "user-plan-snapshot")
    public void clearPlanSnapshotByEmail(String email) { }
    
}
