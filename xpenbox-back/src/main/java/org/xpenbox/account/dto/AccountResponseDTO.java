package org.xpenbox.account.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Account details.
 * @param resourceCode          The unique resource code of the account.
 * @param name                  The name of the account.
 * @param balance               The balance of the account as a BigDecimal.
 * @param state                 The state of the account (active/inactive).
 * @param closingDateTimestamp  The closing date of the account as a timestamp.
 */
@RegisterForReflection
public record AccountResponseDTO (
    String resourceCode,
    String name,
    BigDecimal balance,
    Boolean state,
    Long closingDateTimestamp
) { }
