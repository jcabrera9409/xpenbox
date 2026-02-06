package org.xpenbox.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

import org.xpenbox.creditcard.dto.CreditCardResponseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for the current period dashboard information.
 * Includes financial metrics and associated credit card details.
 * @param currentBalance The current balance.
 * @param openingBalance The opening balance.
 * @param delta The change in balance during the current period.
 * @param creditUsed The amount of credit used in the current period.
 * @param creditLimit The credit limit for the current period.
 * @param creditCards A list of credit card details associated with the current period.
 */
@RegisterForReflection
public record DashboardCurrentPeriodDTO (
    @JsonProperty("current_balance") 
    BigDecimal currentBalance,
    
    @JsonProperty("opening_balance")
    BigDecimal openingBalance,

    @JsonProperty("delta")
    BigDecimal delta,

    @JsonProperty("credit_used")
    BigDecimal creditUsed,

    @JsonProperty("credit_limit")
    BigDecimal creditLimit,

    @JsonProperty("credit_cards")
    List<CreditCardResponseDTO> creditCards
) { }
