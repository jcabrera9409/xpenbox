package org.xpenbox.payment.provider.mercadopago.client.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for creating a subscription approval request in MercadoPago.
 * @param reason The reason for the subscription.
 * @param external_reference An external reference for the subscription.
 * @param payer_email The email of the payer.
 * @param auto_recurring The auto-recurring payment details.
 * @param back_url The URL to redirect the user after the approval process.
 */
public record MPApprovalRequestDTO (
    String reason,
    String external_reference,
    String payer_email,
    AutoRecurring auto_recurring,
    String back_url
) { 
    public static AutoRecurring generateMonthlyAutoRecurring(BigDecimal amount, String currency) {
        return new AutoRecurring(1, "months", amount, currency);
    }
}

record AutoRecurring (
    Integer frequency,
    String frequency_type,
    BigDecimal transaction_amount,
    String currency_id
) { }