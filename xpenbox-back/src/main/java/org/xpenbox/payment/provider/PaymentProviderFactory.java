package org.xpenbox.payment.provider;

import org.xpenbox.payment.enums.PaymentProviderType;
import org.xpenbox.payment.provider.mercadopago.MercadoPagoPaymentProvider;

import jakarta.inject.Singleton;

@Singleton
public class PaymentProviderFactory {
    
    private final MercadoPagoPaymentProvider mercadoPagoPaymentProvider;

    public PaymentProviderFactory(
        MercadoPagoPaymentProvider mercadoPagoPaymentProvider
    ) {
        this.mercadoPagoPaymentProvider = mercadoPagoPaymentProvider;
    }

    public PaymentProvider getPaymentProvider(PaymentProviderType providerType) {
        return switch (providerType) {
            case MERCADOPAGO -> mercadoPagoPaymentProvider;
            default -> throw new IllegalArgumentException("Unsupported payment provider: " + providerType);
        };
    }
}
