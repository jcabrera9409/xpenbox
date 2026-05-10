package org.xpenbox.notifications.service.impl;

import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.common.service.impl.GenericServiceImpl;
import org.xpenbox.notifications.dto.DeviceTokenCreateDTO;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.notifications.mapper.DeviceTokenMapper;
import org.xpenbox.notifications.repository.DeviceTokenRepository;
import org.xpenbox.notifications.service.IDeviceTokenService;
import org.xpenbox.user.repository.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service implementation for DeviceToken entity operations.
 */
@ApplicationScoped
public class DeviceTokenService extends GenericServiceImpl<DeviceToken, DeviceTokenCreateDTO, DeviceTokenCreateDTO, DeviceTokenCreateDTO> implements IDeviceTokenService {

    private final UserRepository userRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final DeviceTokenMapper deviceTokenMapper;

    public DeviceTokenService(
        UserRepository userRepository,
        DeviceTokenRepository deviceTokenRepository,
        DeviceTokenMapper deviceTokenMapper
    ) {
        this.userRepository = userRepository;
        this.deviceTokenRepository = deviceTokenRepository;
        this.deviceTokenMapper = deviceTokenMapper;
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
    
}
