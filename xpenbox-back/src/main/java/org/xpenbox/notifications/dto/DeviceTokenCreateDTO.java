package org.xpenbox.notifications.dto;

import org.xpenbox.notifications.entity.DeviceToken.Platform;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data Transfer Object for creating new DeviceToken records.
 * @param token     The device token string used for push notifications.
 * @param platform  The platform of the device (e.g., ANDROID, IOS).
 */
@RegisterForReflection
public record DeviceTokenCreateDTO (

    @NotNull(message = "Token must not be null")
    @NotBlank(message = "Token must not be blank")
    @Max(value = 500, message = "Token must not exceed 500 characters")
    String token,

    @NotNull(message = "Platform must not be null")
    Platform platform
) { }
