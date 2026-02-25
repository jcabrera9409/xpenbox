import { FeatureCodeEnum, PlanFeatureResponseDTO } from "./plan-feature.response.dto";

export interface PlanResponseDTO {
    resourceCode: string;
    name: string;
    description: string;
    price: number;
    currency: string;
    billingCycle: BillingCycle;
    features: Record<FeatureCodeEnum, PlanFeatureResponseDTO>;
}

export enum BillingCycle {
    MONTHLY = "MONTHLY",
    YEARLY = "YEARLY"
}