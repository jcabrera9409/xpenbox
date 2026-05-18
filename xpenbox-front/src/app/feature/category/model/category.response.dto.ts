/**
 * DTO representing the response structure for a category.
 * Includes resource code, name, color, amount, budget, hasBudget, last used date timestamp, usage count, and state.
 */
export interface CategoryResponseDTO {
    resourceCode: string;
    name: string;
    color: string;
    amount: number;
    budget: number;
    hasBudget: boolean;
    lastUsedDateTimestamp: number;
    usageCount: number;
    state: boolean;
}
