import { signal } from "@angular/core";
import { CategoryResponseDTO } from "../model/category.response.dto";
import { CategoryBudgetUsageRequestDTO } from "../model/categorybudgetusage.request.dto";

/**
 * State management for category-related data.
 * @returns An object containing signals for loading state, error messages, category list, and budget usage.
 */
export const categoryState = {
    categories: signal<CategoryResponseDTO[]>([]),
    categoriesBudgetUsage: signal<CategoryBudgetUsageRequestDTO[] | null>(null),

    isLoadingGetList: signal<boolean>(false),
    errorGetList: signal<string | null>(null),

    isLoadingGetBudgetUsage: signal<boolean>(false),
    errorGetBudgetUsage: signal<string | null>(null),

    isLoadingGetCategory: signal<boolean>(false),
    errorGetCategory: signal<string | null>(null),

    isLoadingSendingCategory: signal<boolean>(false),
    errorSendingCategory: signal<string | null>(null)
};
