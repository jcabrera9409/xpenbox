package org.xpenbox.common.service;

import java.util.List;

/**
 * Generic Service Interface
 * @param <T> The entity type.
 * @param <C> The create DTO type.
 * @param <U> The update DTO type.
 * @param <R> The response DTO type.
 */
public interface IGenericService<T, C, U, R> {
    
    /**
     * Create a new entity
     * @param entityCreateDTO the entity creation data transfer object
     * @param userEmail the email of the user creating the entity
     * @return the created entity response data transfer object
     */
    R create(C entityCreateDTO, String userEmail);

    /**
     * Update an existing entity
     * @param resourceCode the resource code of the entity to be updated
     * @param entityUpdateDTO the entity update data transfer object
     * @param userEmail the email of the user updating the entity
     * @return the updated entity response data transfer object
     */
    R update(String resourceCode, U entityUpdateDTO, String userEmail);

    /**
     * Get an entity by its resource code
     * @param resourceCode the resource code of the entity
     * @param userEmail the email of the user requesting the entity
     * @return the entity response data transfer object
     */
    R getByResourceCode(String resourceCode, String userEmail);

    /**
     * Get all entities for a user
     * @param userEmail the email of the user
     * @return a list of entity response data transfer objects
     */
    List<R> getAll(String userEmail);
}
