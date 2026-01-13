package org.xpenbox.creditcard.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for Credit Card response.
 * @param resourceCode   Unique code identifying the credit card.
 * @param name           Name of the credit card.
 * @param creditLimit    Credit limit of the card.
 * @param currentBalance Current balance on the credit card.
 * @param state          State of the credit card (active/inactive).
 * @param billingDay     Billing day of the month.
 * @param paymentDay     Payment day of the month.
 * @param closingDate    Closing date of the credit card statement.
 */
@RegisterForReflection
public record CreditCardResponseDTO (
    String resourceCode,
    String name,
    BigDecimal creditLimit,
    BigDecimal currentBalance,
    Boolean state,
    Byte billingDay,
    Byte paymentDay,
    LocalDateTime closingDate
) { }
