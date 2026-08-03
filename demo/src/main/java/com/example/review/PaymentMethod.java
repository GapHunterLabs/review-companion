package com.example.review;

import java.math.BigDecimal;

public class PaymentMethod {
    private final PaymentType type;
    private final BigDecimal surcharge;

    public PaymentMethod(PaymentType type, BigDecimal surcharge) {
        this.type = type;
        this.surcharge = surcharge;
    }

    public PaymentType getType() { return type; }
    public BigDecimal getSurcharge() { return surcharge; }
}
