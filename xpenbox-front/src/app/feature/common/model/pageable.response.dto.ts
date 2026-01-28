/**
 * Dato Transfer Object for pageable responses from the API.
 * @page The number of the current page (0-indexed).
 * @size The size of the page (number of items per page).
 * @totalElements The total number of elements across all pages.
 * @totalPages The total number of pages available.
 * @content The array of items for the current page.
 */
export interface PageableResponseDTO<T> {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    content: T[];
}