package org.xpenbox.notifications.service.impl;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.notifications.dto.DeviceTokenCreateDTO;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.notifications.mapper.DeviceTokenMapper;
import org.xpenbox.notifications.repository.DeviceTokenRepository;
import org.xpenbox.notifications.service.IDeviceTokenService;
import org.xpenbox.notifications.service.IPushNotificationService;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service implementation for DeviceToken entity operations.
 */
@ApplicationScoped
public class DeviceTokenService extends GenericServiceImpl<DeviceToken, DeviceTokenCreateDTO, DeviceTokenCreateDTO, DeviceTokenCreateDTO> implements IDeviceTokenService {
    private static final Logger LOG = Logger.getLogger(DeviceTokenService.class);

    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceTokenMapper deviceTokenMapper;
    private final IPushNotificationService pushNotificationService;

    public DeviceTokenService(
        UserRepository userRepository,
        DeviceTokenRepository deviceTokenRepository,
        DeviceTokenMapper deviceTokenMapper,
        IPushNotificationService pushNotificationService
    ) {
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.deviceTokenMapper = deviceTokenMapper;
        this.pushNotificationService = pushNotificationService;
    }

    @Override
    protected String getEntityName() {
        return "DeviceToken";
    }

    @Override
    protected UserRepository getUserRepository() {
        return userRepository;
    }

    @Override
    protected GenericRepository<DeviceToken> getGenericRepository() {
        return deviceTokenRepository;
    }

    @Override
    protected GenericMapper<DeviceToken, DeviceTokenCreateDTO, DeviceTokenCreateDTO, DeviceTokenCreateDTO> getGenericMapper() {
        return deviceTokenMapper;
    }

    @Override
    public DeviceTokenCreateDTO create(DeviceTokenCreateDTO createDTO, String userEmail) {
        LOG.infof("Creating entity %s for user email: %s", getEntityName(), userEmail);
        User user = validateAndGetUser(userEmail);
        
        DeviceToken existingToken = deviceTokenRepository.findByToken(createDTO.token())
            .orElse(null);
        
        if (existingToken != null) {
            if (existingToken.getUser().id == user.id) {
                LOG.infof("Device token already exists for user %s, returning existing token", userEmail);
                return getGenericMapper().toDTO(existingToken);
            } 

            deviceTokenRepository.deleteByToken(createDTO.token());
            LOG.infof("Device token already exists for another user, deleted existing token and creating new token for user %s", userEmail);
        }

        DeviceToken newEntity = getGenericMapper().toEntity(createDTO, user);

        getGenericRepository().persist(newEntity);
        LOG.infof("%s created for user %s", getEntityName(), userEmail);

        return getGenericMapper().toDTO(newEntity);
    }
 
    @Override
    public void removeDeviceToken(String token) {
        LOG.infof("Removing device token: %s", token);
        deviceTokenRepository.deleteByToken(token);
        LOG.infof("Device token removed successfully");
    }

    @Override
    public void sendTestNotification(String userEmail) {
        LOG.infof("Sending test notification to user: %s", userEmail);
        User user = validateAndGetUser(userEmail);
        List<DeviceToken> deviceTokens = deviceTokenRepository.findAllByUserId(user.id);
        for (DeviceToken deviceToken : deviceTokens) {
            LOG.infof("Sending test notification to device token: %s", deviceToken.getToken());
            pushNotificationService.sendPushNotification(
                deviceToken.getToken(), 
                "Test Notification: " + "Tu tarjeta vence hoy 💳" , 
                "Realiza tu pago a tiempo para evitar intereses y cargos adicionales."
            );
        }
    }
}
