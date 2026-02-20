package org.xpenbox.payment.enums;

public enum FeatureCodeEnum {

    // =============================
    // DASHBOARD
    // =============================
    DASHBOARD_ADVANCED_FILTERS, // e.g., filter transactions month, last month, last 3 months, etc.
    DASHBOARD_TRENDS, 
    DASHBOARD_COMPARATIVES, 

    // =============================
    // LIMITS
    // =============================
    ACCOUNTS_LIMIT, // e.g., number of linked accounts
    CREDIT_CARDS_LIMIT, // e.g., number of linked credit cards
    CATEGORIES_LIMIT, // e.g., number of custom categories
    TRANSACTIONS_LIMIT, // e.g., number of transactions that can be created in a month
    TRANSACTION_HISTORY_MONTHS, // e.g., number of months of transaction history that can be accessed

    // =============================
    // AUTOMATIONS
    // =============================
    RECURRING_INCOME,
    RECURRING_EXPENSES,
    AUTO_CHARGES,

    // =============================
    // BUDGET & ALERTS
    // =============================
    CATEGORY_BUDGETS,
    SMART_ALERTS,
    LOW_BALANCE_ALERTS,
    CREDIT_USAGE_ALERTS,

    // =============================
    // EXPORTS
    // =============================
    EXPORT_CSV,
    EXPORT_EXCEL,

    // =============================
    // MULTICURRENCY
    // =============================
    MULTI_CURRENCY_SUPPORT,
    EXCHANGE_RATE_AUTO,

    // =============================
    // ADVANCED
    // =============================
    ADVANCED_TRANSACTION_SEARCH, // e.g., search transactions by amount, category, date range, etc.
    TAGS_IN_TRANSACTIONS,

    // =============================
    // AI
    // =============================
    AI_INSIGHTS,
    AI_PROJECTIONS,
    FINANCIAL_SCORE
}