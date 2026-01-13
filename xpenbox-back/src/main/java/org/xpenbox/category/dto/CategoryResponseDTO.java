package org.xpenbox.category.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Category details.
 * @param resourceCode the unique resource code of the category
 * @param name         the name of the category
 * @param color        the color associated with the category
 * @param state        the state of the category
 */
@RegisterForReflection
public record CategoryResponseDTO (
    String resourceCode,
    String name,
    String color,
    Boolean state
) { }
