package org.xpenbox.notifications.service;

import org.xpenbox.common.service.IGenericService;
import org.xpenbox.notifications.dto.DeviceTokenCreateDTO;
import org.xpenbox.notifications.entity.DeviceToken;

/**
 * Service interface for DeviceToken entity operations.
 */
public interface IDeviceTokenService extends IGenericService<DeviceToken, DeviceTokenCreateDTO, DeviceTokenCreateDTO, DeviceTokenCreateDTO> {
    void removeDeviceToken(String token);
    void sendTestNotification(String userEmail);
}
