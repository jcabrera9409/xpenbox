package org.xpenbox.payment.repository;

import java.util.Optional;

import org.jboss.logging.Logger;
import org.xpenbox.payment.entity.Plan;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for managing Plan entities.
 */
@ApplicationScoped
public class PlanRepository implements PanacheRepository<Plan> {
    private static final Logger LOG = Logger.getLogger(PlanRepository.class);

    public Optional<Plan> findByResourceCode(String resourceCode) {
        LOG.infof("Finding plan with resource code: %s", resourceCode);
        return find("resourceCode", resourceCode).firstResultOptional();
    }
    
}
