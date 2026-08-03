package com.example.review;

public class ShippingOptions {
    private final ShippingMethod method;

    public ShippingOptions(ShippingMethod method) {
        this.method = method;
    }

    public ShippingMethod getMethod() { return method; }
}
