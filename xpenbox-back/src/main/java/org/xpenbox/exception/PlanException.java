package org.xpenbox.exception;

import org.xpenbox.payment.enums.FeatureCodeEnum;

import jakarta.ws.rs.core.Response;

/**
 * Exception thrown when a user has exceeded the limits of their current plan. This exception indicates that the user cannot create more accounts, credit cards, or categories because they have reached the maximum allowed by their subscription plan. It maps to HTTP 403 Forbidden status.
 */
public class PlanException extends BussinessException {

    private FeatureCodeEnum featureCode;
    private Long limit;
    private Long currentUsage;
    private Boolean enabled;

    public PlanException(String message) {
        super(message, Response.Status.FORBIDDEN.getStatusCode());
    }

    public PlanException(
        String message,
        FeatureCodeEnum featureCode
    ) {
        super(message, Response.Status.FORBIDDEN.getStatusCode());
        this.featureCode = featureCode;
        this.enabled = false;
    }

    public PlanException(
        String message, 
        FeatureCodeEnum featureCode, 
        Long limit, 
        Long currentUsage
    ) {
        super(message, Response.Status.FORBIDDEN.getStatusCode());
        this.featureCode = featureCode;
        this.limit = limit;
        this.currentUsage = currentUsage;
        this.enabled = true;
    }

    public FeatureCodeEnum getFeatureCode() {
        return featureCode;
    }

    public Long getLimit() {
        return limit;
    }

    public Long getCurrentUsage() {
        return currentUsage;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
}
