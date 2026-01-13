package org.xpenbox.user.mapper;

import org.jboss.logging.Logger;
import org.xpenbox.user.dto.UserCreateDTO;
import org.xpenbox.user.dto.UserResponseDTO;
import org.xpenbox.user.entity.User;

/**
 * Mapper class for converting between User entities and DTOs.
 */
public class UserMapper {
    private static final Logger LOG = Logger.getLogger(UserMapper.class);

    /**
     * Converts a User entity to a UserResponseDTO.
     *
     * @param entity the User entity to convert
     * @return the corresponding UserResponseDTO
     */
    public static UserResponseDTO toDTO(User entity) {
        LOG.infof("Mapping User entity to DTO: %s", entity);
        UserResponseDTO dto = new UserResponseDTO(
            entity.getUsername(),
            entity.getEmail(),
            entity.getCurrency(),
            entity.getVerified()
        );
        return dto;
    }

    /**
     * Converts a UserCreateDTO to a User entity.
     *
     * @param dto the UserCreateDTO to convert
     * @return the corresponding User entity
     */
    public static User toEntity(UserCreateDTO dto) {
        LOG.infof("Mapping UserCreateDTO to entity: %s", dto);
        User entity = new User();
        entity.setUsername(dto.username());
        entity.setEmail(dto.email());
        entity.setPassword(dto.password());
        entity.setCurrency(dto.currency());
        return entity;
    }
}
