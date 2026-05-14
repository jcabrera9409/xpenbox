package org.xpenbox.notifications.repository;

import java.util.List;
import java.util.Optional;

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

    /**
     * Finds all device tokens with state true and the specified platform.
     * @param platform The platform to filter device tokens by (e.g., ANDROID, IOS). Only tokens with state true will be returned.
     * @return A list of DeviceToken entities that match the specified criteria.
     */
    public List<DeviceToken> findAllByStateTrueAndPlatform(Platform platform) {
        LOG.info("Fetching all device tokens with state true and platform " + platform + " from the database");
        return find("state = :state and platform = :platform", 
            Parameters.with("state", true).and("platform", platform)
        ).list();
    }

    public Optional<DeviceToken> findByToken(String token) {
        LOG.infof("Finding device token with token: %s", token);
        return find("token", token).firstResultOptional();
    }

    /**
     * Deletes a device token by its token value.
     * @param token The token value of the device token to be deleted. The method will delete the device token with the specified token value from the database.
     */
    public void deleteByToken(String token) {
        LOG.infof("Deleting device token with token: %s", token);
        delete("token", token);
    }

    /**
     * Finds all device tokens for a specific user ID.
     * @param userId The ID of the user to find device tokens for. The method will return a list of device tokens associated with the specified user ID.
     * @return A list of DeviceToken entities associated with the specified user ID. If no device tokens are found for the user ID, an empty list will be returned.
     */
    public List<DeviceToken> findAllByUserId(Long userId) {
        LOG.infof("Finding all device tokens for user id: %s", userId);
        return find("user.id", userId).list();
    }
}
