package org.xpenbox.category.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new Category.
 * @param name  the name of the category
 * @param color the color associated with the category
 * @param budget the budget of the category
 * @param hasBudget indicates if the category has a budget
 */
@RegisterForReflection
public record CategoryCreateDTO (
    @NotNull(message = "Resource code must not be null")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    String name,

    @NotNull(message = "Color must not be null")
    @Size(min = 3, max = 20, message = "Color must be between 3 and 20 characters")
    String color,

    @NotNull(message = "Budget must not be null")
    @Min(value = 0, message = "Budget must be a positive value")
    BigDecimal budget,

    @NotNull(message = "HasBudget must not be null")
    Boolean hasBudget
) { }
