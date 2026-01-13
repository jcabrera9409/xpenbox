package org.xpenbox.creditcard.dto;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for updating an existing CreditCard.
 * @param name          The name of the credit card.
 * @param creditLimit   The credit limit of the credit card.
 * @param billingDay    The billing day of the credit card.
 * @param paymentDay    The payment day of the credit card.
 * @param state         The active state of the credit card.
 */
@RegisterForReflection
public record CreditCardUpdateDTO (
    @Size(min = 3, max = 150, message = "Name must be between 3 and 150 characters")
    String name,

    @Min(value = 1, message = "Credit limit must be at least 1")
    BigDecimal creditLimit,

    @Min(value = 1, message = "Billing day must be between 1 and 31")
    @Max(value = 31, message = "Billing day must be between 1 and 31")
    Byte billingDay,

    @Min(value = 1, message = "Payment day must be between 1 and 31")
    @Max(value = 31, message = "Payment day must be between 1 and 31")
    Byte paymentDay,

    Boolean state
) { }
