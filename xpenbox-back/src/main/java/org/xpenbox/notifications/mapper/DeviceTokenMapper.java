package org.xpenbox.notifications.mapper;

import java.util.List;

import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.notifications.dto.DeviceTokenCreateDTO;
import org.xpenbox.notifications.entity.DeviceToken;
import org.xpenbox.user.entity.User;

import jakarta.inject.Singleton;

/**
 * Mapper class for converting between DeviceToken entities and DTOs.
 */
@Singleton
public class DeviceTokenMapper implements GenericMapper<DeviceToken, DeviceTokenCreateDTO, DeviceTokenCreateDTO, DeviceTokenCreateDTO> {

    @Override
    @Deprecated
    public DeviceTokenCreateDTO toDTO(DeviceToken entity) {
        throw new UnsupportedOperationException("Unimplemented method 'toDTO'");
    }

    @Override
    @Deprecated
    public DeviceTokenCreateDTO toSimpleDTO(DeviceToken entity) {
        throw new UnsupportedOperationException("Unimplemented method 'toSimpleDTO'");
    }

    @Override
    @Deprecated
    public List<DeviceTokenCreateDTO> toDTOList(List<DeviceToken> entities) {
        throw new UnsupportedOperationException("Unimplemented method 'toDTOList'");
    }

    @Override
    public DeviceToken toEntity(DeviceTokenCreateDTO createDto, User user) {
        DeviceToken entity = new DeviceToken();
        entity.setToken(createDto.token());
        entity.setPlatform(createDto.platform());
        entity.setState(true);
        entity.setUser(user);
        return entity;
    }

    @Override
    @Deprecated
    public boolean updateEntity(DeviceTokenCreateDTO updateDto, DeviceToken entity) {
        throw new UnsupportedOperationException("Unimplemented method 'updateEntity'");
    }
    
}
