package org.xpenbox.common.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * APIResponseDTO is a generic data transfer object for API responses.
 * @param success Indicates if the API call was successful
 * @param message Message providing additional information about the response
 * @param data The actual response data
 * @param statusCode HTTP status code of the response
 * @param timestamp Timestamp indicating when the response was created
 */
@RegisterForReflection
public record APIResponseDTO<T>(
    boolean success,
	String message,
	T data,
	int statusCode,
	Long timestamp
) {
    /**
     * Create a successful API response
     * @param <T> Type of the data
     * @param message Success message
     * @param data Response data
     * @param statusCode HTTP status code
     * @return APIResponseDTO representing a successful response
     */
    public static <T> APIResponseDTO<T> success(String message, T data, int statusCode) {
        return new APIResponseDTO<>(true, message, data, statusCode, System.currentTimeMillis());
    }

    /**
     * Create an error API response
     * @param <T> Type of the data
     * @param message Error message
     * @param statusCode HTTP status code
     * @return APIResponseDTO representing an error response
     */
    public static <T> APIResponseDTO<T> error(String message, int statusCode) {
        return new APIResponseDTO<>(false, message, null, statusCode, System.currentTimeMillis());
    }
 }
