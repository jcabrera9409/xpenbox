package org.xpenbox.common.dto;

public record APIResponseDTO<T>(
    boolean success,
	String message,
	T data,
	int statusCode,
	Long timestamp
) {
    public static <T> APIResponseDTO<T> success(String message, T data, int statusCode) {
        return new APIResponseDTO<>(true, message, data, statusCode, System.currentTimeMillis());
    }

    public static <T> APIResponseDTO<T> error(String message, int statusCode) {
        return new APIResponseDTO<>(false, message, null, statusCode, System.currentTimeMillis());
    }
 }
