import { computed, Injectable } from '@angular/core';
import { subscriptionState } from './subscription.state';
import { FeatureCodeEnum } from '../model/plan-feature.response.dto';

@Injectable({
  providedIn: 'root',
})
export class EntitlementService {

  readonly canUseDashboardAvancedFilters = computed(() => {
    const subscription = subscriptionState.subscription();
    const feature = FeatureCodeEnum.DASHBOARD_ADVANCED_FILTERS;

    if (!subscription) return false;

    return subscription.plan.features[feature]?.isEnabled ?? false;
  });


}
