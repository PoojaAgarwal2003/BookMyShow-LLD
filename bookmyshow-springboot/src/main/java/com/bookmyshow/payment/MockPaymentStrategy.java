package com.bookmyshow.payment;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.UUID;

@Component("MOCK")
public class MockPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentOutcome pay(BigDecimal amount, String idempotencyKey) {
        // Deterministic local simulation: amounts ending in .99 simulate failure for
        // demos.
        boolean success = amount.remainder(BigDecimal.ONE).compareTo(new BigDecimal("0.99")) != 0;
        return new PaymentOutcome(success, "MOCK-" + UUID.randomUUID(),
                success ? "Payment approved" : "Simulated payment decline");
    }
}
