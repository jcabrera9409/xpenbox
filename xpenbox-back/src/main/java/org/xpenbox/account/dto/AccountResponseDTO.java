package org.xpenbox.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Account details.
 * @param resourceCode The unique resource code of the account.
 * @param name         The name of the account.
 * @param balance      The balance of the account as a string.
 * @param closingDate  The closing date of the account as a string.
 */
@RegisterForReflection
public record AccountResponseDTO (
    String resourceCode,
    String name,
    BigDecimal balance,
    LocalDateTime closingDate
) { }
