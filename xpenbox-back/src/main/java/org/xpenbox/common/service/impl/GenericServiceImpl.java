package org.xpenbox.common.service.impl;

import java.util.List;

import org.jboss.logging.Logger;
import org.xpenbox.common.mapper.GenericMapper;
import org.xpenbox.common.repository.GenericRepository;
import org.xpenbox.common.service.IGenericService;
import org.xpenbox.exception.ResourceNotFoundException;
import org.xpenbox.exception.UnauthorizedException;
import org.xpenbox.user.entity.User;
import org.xpenbox.user.repository.UserRepository;

/**
 * Generic Service Implementation
 */
public abstract class GenericServiceImpl<T, C, U, R> implements IGenericService<T, C, U, R> {
    private static final Logger LOG = Logger.getLogger(GenericServiceImpl.class);

    protected abstract String getEntityName();
    protected abstract UserRepository getUserRepository();
    protected abstract GenericRepository<T> getGenericRepository();
    protected abstract GenericMapper<T, C, U, R> getGenericMapper();

    @Override
    public R create(C entityCreateDTO, String userEmail) {
        LOG.infof("Creating entity %s for user email: %s", getEntityName(), userEmail);
        User user = validateAndGetUser(userEmail);
        
        T newEntity = getGenericMapper().toEntity(entityCreateDTO, user);

        getGenericRepository().persist(newEntity);
        LOG.infof("%s created ", getEntityName());

        return getGenericMapper().toDTO(newEntity);
    }

    @Override
    public R update(String resourceCode, U entityUpdateDTO, String userEmail) {
        LOG.infof("Updating entity %s with resource code %s for user email: %s", getEntityName(), resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);
        
        T existingEntity = getGenericRepository().findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("%s not found with resource code: %s for user email: %s", getEntityName(), resourceCode, userEmail);
                throw new ResourceNotFoundException(getEntityName() + " not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });
        
        boolean updated = getGenericMapper().updateEntity(entityUpdateDTO, existingEntity);
        if (updated) {
            getGenericRepository().persist(existingEntity);
            LOG.infof("%s updated with resource code: %s", getEntityName(), resourceCode);
        } else {
            LOG.infof("No changes detected for %s with resource code: %s", getEntityName(), resourceCode);
        }

        return getGenericMapper().toDTO(existingEntity);
    }

    @Override
    public R getByResourceCode(String resourceCode, String userEmail) {
        LOG.infof("Retrieving entity %s with resource code %s for user email: %s", getEntityName(), resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);

        T existingEntity = getGenericRepository().findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("%s not found with resource code: %s for user email: %s", getEntityName(), resourceCode, userEmail);
                throw new ResourceNotFoundException(getEntityName() + " not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });

        return getGenericMapper().toDTO(existingEntity);
    }

    @Override
    public List<R> getAll(String userEmail) {
        LOG.infof("Retrieving all entities %s for user email: %s", getEntityName(), userEmail);
        User user = validateAndGetUser(userEmail);
        
        List<T> entities = getGenericRepository().findAllByUserId(user.id);
        LOG.infof("Found %d entities %s for user email: %s", entities.size(), getEntityName(), userEmail);
        return getGenericMapper().toDTOList(entities);
    }

    @Override
    public void deleteByResourceCode(String resourceCode, String userEmail) {
        LOG.infof("Deleting entity %s with resource code %s for user email: %s", getEntityName(), resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);
        T existingEntity = getGenericRepository().findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("%s not found with resource code: %s for user email: %s", getEntityName(), resourceCode, userEmail);
                throw new ResourceNotFoundException(getEntityName() + " not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });
        getGenericRepository().delete(existingEntity);
        LOG.infof("%s deleted with resource code: %s", getEntityName(), resourceCode);
    }
    
    protected User validateAndGetUser(String userEmail) {
        return getUserRepository().findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                throw new UnauthorizedException("User not found with email: " + userEmail); 
            });
    }
}
