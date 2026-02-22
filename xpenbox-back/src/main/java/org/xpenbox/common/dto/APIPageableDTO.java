package org.xpenbox.common.dto;

import java.util.List;

import org.xpenbox.transaction.dto.TransactionFilterDTO;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Generic pageable DTO for API responses.
 * @param page the current page number
 * @param size the size of the page
 * @param totalElements the total number of elements
 * @param totalPages the total number of pages
 * @param <T> the type of content in the pageable response
 * @param content the content of the current page
 * @param clipped whether the content is clipped
 * @param filter the filter used for the content, if applicable
 */
@RegisterForReflection
public record APIPageableDTO<T> (
    Integer page,
    Integer size,
    Integer totalElements,
    Integer totalPages,
    List<T> content,
    Boolean clipped,
    TransactionFilterDTO filter
) { 
    /**
     * Generate a pageable DTO.
     * @param <T> the type of content
     * @param pageNumber the current page number
     * @param pageSize the size of the page
     * @param totalElements the total number of elements
     * @param content the content of the current page
     * @param clipped whether the content is clipped
     * @param filter the filter used for the content, if applicable
     * @return a pageable DTO containing the provided information
     */
    public static <T> APIPageableDTO<T> generatePageableDTO(Integer pageNumber, Integer pageSize, Integer totalElements, List<T> content, Boolean clipped, TransactionFilterDTO filter) {
        Integer totalPages = 0;
        if (pageNumber != null && pageSize != null) {
            totalPages = (int) Math.ceil((double) totalElements / pageSize);
        }
        return new APIPageableDTO<>(pageNumber, pageSize, totalElements, totalPages, content, clipped, filter);
    }
}
