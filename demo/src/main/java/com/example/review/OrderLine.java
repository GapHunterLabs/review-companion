package com.example.review;

import java.math.BigDecimal;

public class OrderLine {
    private final BigDecimal unitPrice;
    private final int quantity;

    public OrderLine(BigDecimal unitPrice, int quantity) {
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
}
