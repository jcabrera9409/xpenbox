package org.xpenbox.transaction.dto;

import java.math.BigDecimal;

import org.xpenbox.account.dto.AccountResponseDTO;
import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.creditcard.dto.CreditCardResponseDTO;
import org.xpenbox.income.dto.IncomeResponseDTO;
import org.xpenbox.transaction.entity.Transaction.TransactionType;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for responding with Transaction details.
 * @param resourceCode Resource code of the transaction.
 * @param description Description of the transaction.
 * @param transactionType Type of the transaction.
 * @param amount Amount involved in the transaction.
 * @param latitude Latitude where the transaction took place.
 * @param longitude Longitude where the transaction took place.
 * @param transactionDateTimestamp Timestamp of the transaction date and time.
 * @param category Associated category details.
 * @param income Associated income details.
 * @param account Associated account details.
 * @param creditCard Associated credit card details.
 * @param destinationAccount Associated destination account details (for transfers).
 */
@RegisterForReflection
public record TransactionResponseDTO (
    String resourceCode,
    String description,
    TransactionType transactionType,
    BigDecimal amount,
    BigDecimal latitude,
    BigDecimal longitude,
    Long transactionDateTimestamp,
    CategoryResponseDTO category,
    IncomeResponseDTO income,
    AccountResponseDTO account,
    CreditCardResponseDTO creditCard,
    AccountResponseDTO destinationAccount
) { }
