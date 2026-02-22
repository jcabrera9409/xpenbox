package org.xpenbox.common.dto;

import org.xpenbox.payment.enums.FeatureCodeEnum;

public record APILimitExceptionDTO (
    String message,
    FeatureCodeEnum featureCode,
    Integer limit,
    Integer currentUsage,
    Boolean enabled
) { }
