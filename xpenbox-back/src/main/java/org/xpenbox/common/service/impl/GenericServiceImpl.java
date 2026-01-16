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

public class GenericServiceImpl<T, C, U, R> implements IGenericService<T, C, U, R> {
    private static final Logger LOG = Logger.getLogger(GenericServiceImpl.class);

    private final String ENTITY_NAME;
    private final UserRepository userRepository;
    private final GenericRepository<T> genericRepository;
    private final GenericMapper<T, C, U, R> genericMapper;

    public GenericServiceImpl() {
        throw new UnsupportedOperationException("Default constructor is not supported. Use parameterized constructor.");
    }

    public GenericServiceImpl(
        String entityName,
        UserRepository userRepository,
        GenericRepository<T> genericRepository,
        GenericMapper<T, C, U, R> genericMapper
    ) {
        this.ENTITY_NAME = entityName;
        this.userRepository = userRepository;
        this.genericRepository = genericRepository;
        this.genericMapper = genericMapper;
    }

    @Override
    public R create(C entityCreateDTO, String userEmail) {
        LOG.infof("Creating entity %s for user email: %s", ENTITY_NAME, userEmail);
        User user = validateAndGetUser(userEmail);
        
        T newEntity = genericMapper.toEntity(entityCreateDTO, user);

        genericRepository.persist(newEntity);
        LOG.infof("%s created ", ENTITY_NAME);

        return genericMapper.toDTO(newEntity);
    }

    @Override
    public R update(String resourceCode, U entityUpdateDTO, String userEmail) {
        LOG.infof("Updating entity %s with resource code %s for user email: %s", ENTITY_NAME, resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);
        
        T existingEntity = genericRepository.findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("%s not found with resource code: %s for user email: %s", ENTITY_NAME, resourceCode, userEmail);
                throw new ResourceNotFoundException(ENTITY_NAME + " not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });
        
        boolean updated = genericMapper.updateEntity(entityUpdateDTO, existingEntity);

        if (updated) {
            genericRepository.persist(existingEntity);
            LOG.infof("%s updated with resource code: %s", ENTITY_NAME, resourceCode);
        } else {
            LOG.infof("No changes detected for %s with resource code: %s", ENTITY_NAME, resourceCode);
        }

        return genericMapper.toDTO(existingEntity);
    }

    @Override
    public R getByResourceCode(String resourceCode, String userEmail) {
        LOG.infof("Retrieving entity %s with resource code %s for user email: %s", ENTITY_NAME, resourceCode, userEmail);
        User user = validateAndGetUser(userEmail);

        T existingEntity = genericRepository.findByResourceCodeAndUserId(resourceCode, user.id)
            .orElseThrow(() -> {
                LOG.errorf("%s not found with resource code: %s for user email: %s", ENTITY_NAME, resourceCode, userEmail);
                throw new ResourceNotFoundException(ENTITY_NAME + " not found with resource code: " + resourceCode + " for user email: " + userEmail); 
            });

        return genericMapper.toDTO(existingEntity);
    }

    @Override
    public List<R> getAll(String userEmail) {
        LOG.infof("Retrieving all entities %s for user email: %s", ENTITY_NAME, userEmail);
        User user = validateAndGetUser(userEmail);
        
        List<T> entities = genericRepository.findAllByUserId(user.id);
        LOG.infof("Found %d entities %s for user email: %s", entities.size(), ENTITY_NAME, userEmail);

        return genericMapper.toDTOList(entities);
    }
    
    private User validateAndGetUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
            .orElseThrow(() -> {
                LOG.errorf("User not found with email: %s", userEmail);
                throw new UnauthorizedException("User not found with email: " + userEmail); 
            });
    }
}
