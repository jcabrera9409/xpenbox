package org.xpenbox.category.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating an existing Category.
 * @param name  the updated name of the category
 * @param color the updated color associated with the category
 * @param state the updated state of the category
 */
@RegisterForReflection
public record CategoryUpdateDTO (
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    String name,

    @Size(min = 3, max = 20, message = "Color must be between 3 and 20 characters")
    String color,


    Boolean state
) { }
