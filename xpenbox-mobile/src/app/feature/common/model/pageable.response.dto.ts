/**
 * Dato Transfer Object for pageable responses from the API.
 * @page The number of the current page (0-indexed).
 * @size The size of the page (number of items per page).
 * @totalElements The total number of elements across all pages.
 * @totalPages The total number of pages available.
 * @content The array of items for the current page.
 * @clipped A boolean indicating whether the content is clipped (i.e., if there are more items available than the current page size).
 * @filter The filter used for the content, if applicable.
 * @param <T> The type of content in the pageable response.
 * @param <F> The type of filter used for the content, if applicable.
 */
export interface PageableResponseDTO<T, F> {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    content: T[];
    clipped: boolean;
    filter: F;
}