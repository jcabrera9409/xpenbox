/**
 * DTO representing the response structure for a category.
 * Includes resource code, name, color, last used date timestamp, usage count, and state.
 */
export interface CategoryResponseDTO {
    resourceCode: string;
    name: string;
    color: string;
    lastUsedDateTimestamp: number;
    usageCount: number;
    state: boolean;
}
