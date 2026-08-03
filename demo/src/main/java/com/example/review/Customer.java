package com.example.review;

import java.math.BigDecimal;

public class Customer {
    private final String email;
    private final BigDecimal taxRate;

    public Customer(String email, BigDecimal taxRate) {
        this.email = email;
        this.taxRate = taxRate;
    }

    public String getEmail() { return email; }
    public BigDecimal getTaxRate() { return taxRate; }
}
