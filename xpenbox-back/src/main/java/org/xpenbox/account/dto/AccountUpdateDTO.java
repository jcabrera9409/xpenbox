package org.xpenbox.account.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating an existing Account.
 * @param name The updated name of the account.
 */
@RegisterForReflection
public record AccountUpdateDTO (
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    String name
) { }
