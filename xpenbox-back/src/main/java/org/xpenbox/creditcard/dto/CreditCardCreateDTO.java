package org.xpenbox.creditcard.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new CreditCard.
 * @param name          The name of the credit card.
 * @param creditLimit   The credit limit of the credit card.
 * @param currentBalance The current balance on the credit card.
 * @param billingDay    The billing day of the credit card.
 * @param paymentDay    The payment day of the credit card.
 */
@RegisterForReflection
public record CreditCardCreateDTO (
    @NotNull(message = "Name cannot be null")
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    String name,

    @NotNull(message = "Credit limit cannot be null")
    @Min(value = 1, message = "Credit limit must be at least 1")
    BigDecimal creditLimit,

    @NotNull(message = "Current balance cannot be null")
    BigDecimal currentBalance,

    @NotNull(message = "Billing day cannot be null")
    @Min(value = 1, message = "Billing day must be between 1 and 31")
    @Max(value = 31, message = "Billing day must be between 1 and 31")
    Byte billingDay,

    @NotNull(message = "Payment day cannot be null")
    @Min(value = 1, message = "Payment day must be between 1 and 31")
    @Max(value = 31, message = "Payment day must be between 1 and 31")
    Byte paymentDay
) { }
