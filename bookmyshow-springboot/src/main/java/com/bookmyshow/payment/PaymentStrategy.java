package com.bookmyshow.payment;

import java.math.BigDecimal;

public interface PaymentStrategy {
    PaymentOutcome pay(BigDecimal amount, String idempotencyKey);

    record PaymentOutcome(boolean successful, String transactionId, String message) {
    }
}
