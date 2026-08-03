package com.example.review;

import java.math.BigDecimal;
import java.util.List;

/**
 * Deliberately clean -- short functions, shallow nesting, guarded
 * dereferences, no TODO/FIXME comments. Used to confirm Review Companion
 * produces zero findings on reasonable code, not just to confirm it CAN
 * find something.
 */
public class ReportGenerator {

    public BigDecimal totalOf(List<OrderLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            total = total.add(line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity())));
        }
        return total;
    }

    public String summaryFor(Customer customer) {
        if (customer == null) {
            return "Unknown customer";
        }
        return "Customer: " + customer.getEmail();
    }

    public boolean isEligibleForFreeShipping(BigDecimal orderTotal, BigDecimal threshold) {
        return orderTotal.compareTo(threshold) >= 0;
    }
}
