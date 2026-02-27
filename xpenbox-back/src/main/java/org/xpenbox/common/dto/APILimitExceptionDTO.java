package org.xpenbox.common.dto;

import org.xpenbox.payment.enums.FeatureCodeEnum;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object (DTO) for representing API limit exceptions. This DTO is used to convey information about plan limits when a user has exceeded the allowed number of accounts, credit cards, or categories based on their subscription plan. It includes details such as the feature code, the limit, the current usage, and whether the feature is enabled.
 * @param message the error message describing the exception
 * @param featureCode the code of the feature that has reached its limit
 * @param limit the maximum allowed value for the feature based on the user's plan
 * @param currentUsage the current usage value for the feature by the user
 * @param enabled a boolean indicating whether the feature is enabled in the user's plan
 */
@RegisterForReflection
public record APILimitExceptionDTO (
    String message,
    FeatureCodeEnum featureCode,
    Long limit,
    Long currentUsage,
    Boolean enabled
) { }
