package org.xpenbox.transaction.dto;

import java.math.BigDecimal;

import org.xpenbox.transaction.entity.Transaction.TransactionType;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for creating a new Transaction.
 * @param transactionType Type of the transaction.
 * @param description Description of the transaction.
 * @param amount Amount involved in the transaction.
 * @param latitude Latitude where the transaction took place.
 * @param longitude Longitude where the transaction took place.
 * @param transactionDateTimestamp Timestamp of the transaction date and time.
 * @param categoryResourceCode Resource code of the associated category.
 * @param incomeResourceCode Resource code of the associated income.
 * @param accountResourceCode Resource code of the associated account.
 * @param creditCardResourceCode Resource code of the associated credit card.
 * @param destinationAccountResourceCode Resource code of the destination account (for transfers).
 */
@RegisterForReflection
public record TransactionCreateDTO (
    @NotNull(message = "Transaction type cannot be null")
    TransactionType transactionType,

    @NotNull(message = "Description cannot be null")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.00", inclusive = false, message = "Amount must be greater than zero")
    BigDecimal amount,

    @DecimalMin(value = "-90.00000000", inclusive = true, message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.00000000", inclusive = true, message = "Latitude must be between -90 and 90")
    BigDecimal latitude,

    @DecimalMin(value = "-180.00000000", inclusive = true, message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.00000000", inclusive = true, message = "Longitude must be between -180 and 180")
    BigDecimal longitude,

    @NotNull(message = "Transaction date cannot be null")
    @Min(value = 1, message = "Transaction date timestamp must be a positive value")
    Long transactionDateTimestamp,

    @Size(min = 1, max = 100, message = "Category resource code must be between 1 and 100 characters")
    String categoryResourceCode,

    @Size(min = 1, max = 100, message = "Income resource code must be between 1 and 100 characters")
    String incomeResourceCode,

    @Size(min = 1, max = 100, message = "Account resource code must be between 1 and 100 characters")
    String accountResourceCode,

    @Size(min = 1, max = 100, message = "Credit card resource code must be between 1 and 100 characters")
    String creditCardResourceCode,

    @Size(min = 1, max = 100, message = "Destination account resource code must be between 1 and 100 characters")
    String destinationAccountResourceCode
) { }
