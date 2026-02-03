package org.xpenbox.common.repository;

import java.util.List;
import java.util.Optional;

import org.jboss.logging.Logger;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

/**
 * Generic Repository class for entities with user association.
 */
public abstract class GenericRepository<T> implements PanacheRepository<T> {
    
    private final Logger LOG;

    public GenericRepository() {
        this.LOG = Logger.getLogger(this.getClass());
    }

    /**
     * Find an entity by its resource code and user ID.
     * @param resourceCode the resource code of the entity
     * @param userId the ID of the user
     * @return an Optional containing the found entity, or empty if not found
     */
    public Optional<T> findByResourceCodeAndUserId(String resourceCode, Long userId) {
        LOG.infof("Finding entity by resource code: %s and user id: %d", resourceCode, userId);
        return find("resourceCode = ?1 and user.id = ?2", resourceCode, userId).firstResultOptional();
    }

    /**
     * Find all entities associated with a specific user ID.
     * @param userId the ID of the user
     * @return a list of entities associated with the user ID
     */
    public List<T> findAllByUserId(Long userId) {
        LOG.infof("Finding all entities for user id: %d", userId);
        return list("user.id", userId);
    }

    /**
     * Delete an entity by its ID and user ID.
     * @param id the ID of the entity
     * @param userId the ID of the user
     */
    public void deleteByIdAndUserId(Long id, Long userId) {
        LOG.infof("Deleting entity by id: %d and user id: %d", id, userId);
        delete("id = ?1 and user.id = ?2", id, userId);
    }
}
