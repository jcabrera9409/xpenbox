package org.xpenbox.notifications.repository;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.notifications.entity.DeviceToken.Platform;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Repository class for DeviceToken entity operations.
 */
@ApplicationScoped
public class DeviceTokenRepository extends GenericRepository<DeviceToken> {
    private static final Logger LOG = Logger.getLogger(DeviceTokenRepository.class.getName());

    public List<DeviceToken> findAllByStateTrueAndPlatform(Platform platform) {
        LOG.info("Fetching all device tokens with state true and platform " + platform + " from the database");
        return find("state = :state and platform = :platform", 
            Parameters.with("state", true).and("platform", platform)
        ).list();
    }
}
