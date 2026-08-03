package com.example.review;

import java.math.BigDecimal;

public class Discount {
    private final boolean percentage;
    private final BigDecimal value;

    public Discount(boolean percentage, BigDecimal value) {
        this.percentage = percentage;
        this.value = value;
    }

    public boolean isPercentage() { return percentage; }
    public BigDecimal getValue() { return value; }
}
