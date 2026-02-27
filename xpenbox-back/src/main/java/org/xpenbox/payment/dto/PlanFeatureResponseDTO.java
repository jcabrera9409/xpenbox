package org.xpenbox.payment.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for PlanFeature response. This DTO is used to transfer data from the PlanFeature entity to the client.
 * @param featureCode The code representing the feature.
 * @param limitValue The limit value associated with the feature, if applicable.
 * @param isEnabled Indicates whether the feature is enabled or not.
 */
@RegisterForReflection
public record PlanFeatureResponseDTO(
    String featureCode,
    Long limitValue,
    Boolean isEnabled
) { }
