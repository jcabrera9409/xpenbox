package org.xpenbox.category.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new Category.
 * @param name  the name of the category
 * @param color the color associated with the category
 */
@RegisterForReflection
public record CategoryCreateDTO (
    @NotNull(message = "Resource code must not be null")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    String name,

    @NotNull(message = "Color must not be null")
    @Size(min = 3, max = 20, message = "Color must be between 3 and 20 characters")
    String color
) { }
