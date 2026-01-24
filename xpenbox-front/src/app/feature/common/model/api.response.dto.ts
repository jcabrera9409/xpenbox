/**
 * Generic API response DTO interface.
 * T represents the type of the data payload.
 * @template T - The type of the data payload.
 * @property {boolean} success - Indicates if the API call was successful.
 * @property {string} message - A message accompanying the response.
 * @property {T} data - The data payload of the response.
 * @property {number} statusCode - The HTTP status code of the response.
 * @property {number} timestamp - The timestamp when the response was generated.
 */
export interface ApiResponseDTO<T> {
    success: boolean;
    message: string;
    data: T;
    statusCode: number;
    timestamp: number;
}