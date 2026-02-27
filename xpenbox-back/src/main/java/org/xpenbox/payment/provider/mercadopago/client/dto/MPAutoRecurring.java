package org.xpenbox.payment.provider.mercadopago.client.dto;

import java.math.BigDecimal;

public record MPAutoRecurring (
    Integer frequency,
    String frequency_type,
    BigDecimal transaction_amount,
    String currency_id
) { }
