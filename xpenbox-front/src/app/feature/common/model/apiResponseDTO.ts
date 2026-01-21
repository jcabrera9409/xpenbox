export interface ApiResponseDTO<T> {
    success: boolean;
    message: string;
    data: T;
    statusCode: number;
    timestamp: number;
}