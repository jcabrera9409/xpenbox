import { PlanResponseDTO } from "./plan.response.dto";

export interface SubscriptionResponseDTO {
    resourceCode: string;
    planPrice: number;
    planCurrency: string;
    startDateTimestamp: number;
    endDateTimestamp: number;
    nextBillingDateTimestamp: number;
    renew: boolean;
    provider: string;
    providerPlanId: string;
    providerSubscriptionId: string;
    status: SubscriptionStatus;
    plan: PlanResponseDTO;
}

export enum SubscriptionStatus {
    PENDING = "PENDING",
    ACTIVE = "ACTIVE",
    PAST_DUE = "PAST_DUE",
    CANCELLED = "CANCELLED",
    EXPIRED = "EXPIRED",
    TRIAL = "TRIAL",
}