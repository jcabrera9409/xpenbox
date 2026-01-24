package org.xpenbox.creditcard.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for Credit Card response.
 * @param resourceCode          Unique code identifying the credit card.
 * @param name                  Name of the credit card.
 * @param creditLimit           Credit limit of the card.
 * @param currentBalance        Current balance on the credit card.
 * @param lastUsedDateTimestamp Timestamp of the last usage date of the credit card.
 * @param usageCount            Number of times the credit card has been used.
 * @param state                 State of the credit card (active/inactive).
 * @param billingDay            Billing day of the month.
 * @param paymentDay            Payment day of the month.
 * @param closingDateTimestamp  Closing date timestamp of the credit card statement.
 */
@RegisterForReflection
public record CreditCardResponseDTO (
    String resourceCode,
    String name,
    BigDecimal creditLimit,
    BigDecimal currentBalance,
    Long lastUsedDateTimestamp,
    Long usageCount,
    Boolean state,
    Byte billingDay,
    Byte paymentDay,
    Long closingDateTimestamp
) { }
