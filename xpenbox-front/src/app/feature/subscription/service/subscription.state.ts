import { signal } from "@angular/core";
import { SubscriptionResponseDTO } from "../model/subscription.response.dto";

export const subscriptionState = {
  isLoading: signal<boolean>(false),
  subscription: signal<SubscriptionResponseDTO | null>(null),
  error: signal<string | null>(null),

  isLoadingSending: signal<boolean>(false),
  errorSending: signal<string | null>(null)
};