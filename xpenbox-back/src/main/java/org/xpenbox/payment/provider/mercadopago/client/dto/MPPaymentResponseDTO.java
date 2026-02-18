package org.xpenbox.payment.provider.mercadopago.client.dto;

import java.math.BigDecimal;

import org.xpenbox.payment.entity.SubscriptionPayment.PaymentStatus;

/**
 * Data Transfer Object (DTO) representing the response received from the MercadoPago API when retrieving the details of a subscription (payment). This DTO captures essential information about the payment, such as its ID, transaction amount, status, currency, approval date, and point of interaction details. The MPPaymentResponseDTO includes nested records to represent the structure of the response data, allowing for easy access to specific fields related to the payment and its associated subscription.
 * @param id The unique identifier of the payment in MercadoPago.
 * @param transaction_amount The amount of the transaction associated with the payment.
 * @param status The current status of the payment (e.g., "approved", "pending", "rejected").
 * @param currency_id The currency code of the transaction amount (e.g., "USD", "ARS").
 * @param date_approved The timestamp when the payment was approved.
 * @param point_of_interaction An object containing details about the point of interaction for the payment, including transaction data such as the subscription ID associated with the payment.
 */
public record MPPaymentResponseDTO (
    Long id,
    BigDecimal transaction_amount,
    String status,
    String currency_id,
    String date_approved,
    MPPaymentPointInteractionDTO point_of_interaction
) { 
    /**
     * Data Transfer Object (DTO) representing the point of interaction details for a payment in the MercadoPago API response. This DTO captures information about the transaction data associated with the payment, such as the subscription ID linked to the payment. The MPPaymentPointInteractionDTO is nested within the MPPaymentResponseDTO to provide a structured representation of the response data from the MercadoPago API.
     * @param transaction_data An object containing transaction data related to the payment, including the subscription ID
     */
    public record MPPaymentPointInteractionDTO (
        MPPaymentPointInteractionTransactionDataDTO transaction_data
    ) { }

    /**
     * Data Transfer Object (DTO) representing the transaction data associated with a payment in the MercadoPago API response. This DTO captures specific details about the transaction, such as the subscription ID linked to the payment. The MPPaymentPointInteractionTransactionDataDTO is nested within the MPPaymentPointInteractionDTO to provide a structured representation of the transaction data in the response from the MercadoPago API.
     * @param subscription_id The unique identifier of the subscription associated with the payment.
     */
    public record MPPaymentPointInteractionTransactionDataDTO (
        String subscription_id
    ) { }

    public static PaymentStatus mapStatus(String mpStatus) {
        return switch (mpStatus.toLowerCase()) {
            case "approved" -> PaymentStatus.APPROVED;
            case "pending" -> PaymentStatus.PENDING;
            case "rejected" -> PaymentStatus.REJECTED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }
}



