package com.example.review;

import java.math.BigDecimal;
import java.util.List;

/**
 * Realistic order-processing service, deliberately containing one
 * instance of each Review Companion rule -- used both for manual
 * inspection and as the source for the plugin's own automated tests.
 */
public class OrderProcessor {

    // Deliberately long method (60+ lines) to trigger LONG_FUNCTION.
    public BigDecimal processOrder(Customer customer, List<OrderLine> lines, ShippingOptions shipping, PaymentMethod payment, Discount discount) {
        // TODO: extract discount calculation into its own service, this method is doing too much
        // TODO: add proper logging around each pricing step
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one line");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (OrderLine line : lines) {
            BigDecimal lineTotal = line.getUnitPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            totalQuantity += line.getQuantity();
        }

        if (totalQuantity <= 0) {
            throw new IllegalArgumentException("Order must contain at least one unit");
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discount != null) {
            if (discount.isPercentage()) {
                if (discount.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    if (discount.getValue().compareTo(BigDecimal.valueOf(100)) <= 0) {
                        // Deliberately deep nesting (4+ levels) to trigger NESTED_CONDITIONAL.
                        discountAmount = subtotal.multiply(discount.getValue()).divide(BigDecimal.valueOf(100));
                    }
                }
            } else {
                discountAmount = discount.getValue();
            }
        }

        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        if (afterDiscount.compareTo(BigDecimal.ZERO) < 0) {
            afterDiscount = BigDecimal.ZERO;
        }

        BigDecimal shippingCost = BigDecimal.ZERO;
        if (shipping.getMethod() == ShippingMethod.EXPRESS) {
            shippingCost = BigDecimal.valueOf(25);
        } else if (shipping.getMethod() == ShippingMethod.STANDARD) {
            shippingCost = BigDecimal.valueOf(10);
        } else if (shipping.getMethod() == ShippingMethod.PICKUP) {
            shippingCost = BigDecimal.ZERO;
        }

        BigDecimal taxRate = customer.getTaxRate();
        BigDecimal taxAmount = afterDiscount.multiply(taxRate);

        BigDecimal total = afterDiscount.add(shippingCost).add(taxAmount);

        if (payment.getType() == PaymentType.CREDIT_CARD) {
            total = total.add(payment.getSurcharge());
        }

        recordOrderHistory(customer, total);
        notifyWarehouse(lines);
        return total;
    }

    // Deliberately unguarded dereference to trigger NULL_DEREFERENCE:
    // `customer.getEmail()` with no preceding null check on `customer`.
    public void sendConfirmation(Customer customer, BigDecimal total) {
        String email = customer.getEmail();
        EmailService.send(email, "Your order total is " + total);
    }

    // Guarded version -- should NOT trigger NULL_DEREFERENCE, proving the
    // detector isn't just flagging every dereference of a parameter.
    public void sendReminderIfPossible(Customer customer) {
        if (customer != null) {
            String email = customer.getEmail();
            EmailService.send(email, "Reminder: complete your order");
        }
    }

    private void recordOrderHistory(Customer customer, BigDecimal total) {
        // no-op for demo purposes
    }

    private void notifyWarehouse(List<OrderLine> lines) {
        // no-op for demo purposes
    }
}
