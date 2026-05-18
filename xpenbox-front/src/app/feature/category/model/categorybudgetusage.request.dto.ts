import { CategoryResponseDTO } from "./category.response.dto";

/**
 * Data Transfer Object for category budget usage information, including the category details, usage count, and budget used.
 * Includes the category as a CategoryResponseDTO, the number of times the category has been used (usageCount), and the total budget used for that category (budgetUsed).
 */
export interface CategoryBudgetUsageRequestDTO {
    category: CategoryResponseDTO,
    usageCount: number,
    budgetUsed: number
}