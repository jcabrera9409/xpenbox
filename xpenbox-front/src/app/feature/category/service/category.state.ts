import { signal } from "@angular/core";
import { CategoryResponseDTO } from "../model/categoryResponseDTO";

/**
 * State management for category-related data.
 * @returns An object containing signals for loading state, error messages, and category list.
 */
export const categoryState = {
    isLoading: signal<boolean>(false),
    error: signal<string | null>(null),
    categories: signal<CategoryResponseDTO[]>([])
};
