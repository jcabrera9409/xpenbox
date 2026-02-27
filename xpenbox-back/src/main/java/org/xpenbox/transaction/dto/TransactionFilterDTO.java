package org.xpenbox.transaction.dto;

import java.time.LocalDateTime;

import org.xpenbox.transaction.entity.Transaction.TransactionType;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for filtering transactions.
 * @param resourceCode Resource code of the transaction
 * @param transactionType Type of the transaction
 * @param description Description of the transaction
 * @param transactionDateFrom Start date for transaction date filter
 * @param transactionDateTo End date for transaction date filter
 * @param categoryResourceCode Resource code of the category
 * @param incomeResourceCode Resource code of the income
 * @param accountResourceCode Resource code of the account
 * @param creditCardResourceCode Resource code of the credit card
 * @param pageNumber Page number for pagination
 * @param pageSize Page size for pagination
 */
@RegisterForReflection
public record TransactionFilterDTO(
    @Size(max = 100, message = "Resource code must not exceed 100 characters")
    String resourceCode,

    TransactionType transactionType,
   
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,

    LocalDateTime transactionDateFrom,

    LocalDateTime transactionDateTo,

    @Size(max = 100, message = "Category resource code must not exceed 100 characters")
    String categoryResourceCode,

    @Size(max = 100, message = "Income resource code must not exceed 100 characters")
    String incomeResourceCode,

    @Size(max = 100, message = "Account resource code must not exceed 100 characters")
    String accountResourceCode,

    @Size(max = 100, message = "Credit card resource code must not exceed 100 characters")
    String creditCardResourceCode,

    @Min(value = 0, message = "Page number must be a non-negative number")
    Integer pageNumber,
    
    @Min(value = 1, message = "Page size must be a positive number")
    Integer pageSize
) {
     /**
      * Compare this TransactionFilterDTO with another instance to check if they are equivalent based on their non-null fields. This method is useful for determining if two filter DTOs represent the same filtering criteria, even if some fields are null in one of the instances.
      * @param other the other TransactionFilterDTO to compare with
      * @return true if the non-null fields of both DTOs are equal, false otherwise
      */
    public Boolean compareTo(TransactionFilterDTO other) {
        if (other == null) {
            return false;
        }
        if (this.transactionType != null && !this.transactionType.equals(other.transactionType)) {
            return false;
        }
        if (this.description != null && !this.description.equals(other.description)) {
            return false;
        }
        if (this.transactionDateFrom != null && !this.transactionDateFrom.equals(other.transactionDateFrom)) {
            return false;
        }
        if (this.transactionDateTo != null && !this.transactionDateTo.equals(other.transactionDateTo)) {
            return false;
        }
        if (this.categoryResourceCode != null && !this.categoryResourceCode.equals(other.categoryResourceCode)) {
            return false;
        }
        return true;
    }
}
