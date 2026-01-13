package org.xpenbox.account.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new Account.
 * @param name    The name of the account.
 * @param balance The initial balance of the account.
 */
@RegisterForReflection
public record AccountCreateDTO (
    @NotNull(message = "Name cannot be null")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    String name,

    @NotNull(message = "Balance cannot be null")
    @Min(value = 0, message = "Balance must be non-negative")
    BigDecimal balance
) { }
