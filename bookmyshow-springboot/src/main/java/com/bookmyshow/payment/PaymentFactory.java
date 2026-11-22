package com.bookmyshow.payment;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PaymentFactory {
    private final Map<String, PaymentStrategy> strategies;

    public PaymentFactory(Map<String, PaymentStrategy> strategies) {
        this.strategies = strategies;
    }

    public PaymentStrategy get(String method) {
        PaymentStrategy strategy = strategies.get(method.toUpperCase());
        if (strategy == null)
            throw new IllegalArgumentException("Unsupported payment method");
        return strategy;
    }
}
