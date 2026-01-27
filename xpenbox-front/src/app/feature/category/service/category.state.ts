import { signal } from "@angular/core";
import { CategoryResponseDTO } from "../model/category.response.dto";

/**
 * State management for category-related data.
 * @returns An object containing signals for loading state, error messages, and category list.
 */
export const categoryState = {
    categories: signal<CategoryResponseDTO[]>([]),

    isLoadingGetList: signal<boolean>(false),
    errorGetList: signal<string | null>(null),

    isLoadingGetCategory: signal<boolean>(false),
    errorGetCategory: signal<string | null>(null),

    isLoadingSendingCategory: signal<boolean>(false),
    errorSendingCategory: signal<string | null>(null)
};
