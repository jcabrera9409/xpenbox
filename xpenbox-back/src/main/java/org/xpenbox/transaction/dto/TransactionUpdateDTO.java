package org.xpenbox.transaction.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for update a new Transaction.
 * @param description Description of the transaction.
 * @param transactionDateTimestamp Timestamp of the transaction date and time.
 * @param categoryResourceCode Resource code of the associated category.
 */
@RegisterForReflection
public record TransactionUpdateDTO(

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description,

    @Min(value = 1, message = "Transaction date timestamp must be a positive value")
    Long transactionDateTimestamp,

    @Size(min = 1, max = 100, message = "Category resource code must be between 1 and 100 characters")
    String categoryResourceCode
) { } 
