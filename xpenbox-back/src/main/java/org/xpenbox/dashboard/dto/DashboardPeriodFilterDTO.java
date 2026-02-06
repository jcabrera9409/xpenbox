package org.xpenbox.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

import org.xpenbox.category.dto.CategoryResponseDTO;
import org.xpenbox.transaction.dto.TransactionResponseDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Data Transfer Object for the dashboard period filter information.
 * Includes financial summaries, category breakdowns, and recent transactions.
 * @param incomeTotal The total income for the period.
 * @param expenseTotal The total expenses for the period.
 * @param netCashflow The net cash flow for the period.
 * @param categories A list of category breakdowns for the period.
 * @param lastTransactions A list of recent transactions for the period.
 */
@RegisterForReflection
public record DashboardPeriodFilterDTO(

    @JsonProperty("income_total")
    BigDecimal incomeTotal,

    @JsonProperty("expense_total")
    BigDecimal expenseTotal,

    @JsonProperty("net_cashflow")
    BigDecimal netCashflow,

    @JsonProperty("categories")
    List<CategoryResponseDTO> categories,

    @JsonProperty("last_transactions")
    List<TransactionResponseDTO> lastTransactions
) { }
