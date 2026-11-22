package com.bookmyshow.payment;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PaymentFactoryTest {
 @Test void resolvesRegisteredPaymentStrategy() {
  PaymentStrategy mock=(amount,key)->new PaymentStrategy.PaymentOutcome(true,"txn","ok");
  PaymentFactory factory=new PaymentFactory(Map.of("MOCK",mock));
  assertSame(mock,factory.get("MOCK"));
 }
 @Test void rejectsUnknownPaymentStrategy() {
  PaymentFactory factory=new PaymentFactory(Map.of());
  assertThrows(IllegalArgumentException.class,()->factory.get("CARD"));
 }
}
