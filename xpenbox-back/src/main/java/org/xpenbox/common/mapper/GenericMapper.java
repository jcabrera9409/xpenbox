package org.xpenbox.common.mapper;

import java.util.List;

/**
 * Generic Mapper interface for converting between entities and DTOs.
 * @param <T> The entity type.
 * @param <C> The create DTO type.
 * @param <U> The update DTO type.
 * @param <R> The response DTO type.
 */
public interface GenericMapper<T, C, U, R> {

    /**
     * Maps entity to response DTO.
     * @param entity The entity to be mapped.
     * @return The corresponding response DTO.
     */
    R toDTO(T entity);
    
    /**
     * Maps list of entities to list of response DTOs.
     * @param entities The list of entities to be mapped.
     * @return The corresponding list of response DTOs.
     */
    List<R> toDTOList(List<T> entities);
    
    /**
     * Maps create DTO to entity.
     * @param createDto The create DTO to be mapped.
     * @return The corresponding entity.
     */
    T toEntity(C createDto);
    
    /**
     * Maps update DTO to existing entity.
     * @param updateDto The update DTO to be mapped.
     * @param entity The existing entity to be updated.
     * @return true if the entity was updated, false otherwise.
     */
    boolean updateEntity(U updateDto, T entity);
    
}
