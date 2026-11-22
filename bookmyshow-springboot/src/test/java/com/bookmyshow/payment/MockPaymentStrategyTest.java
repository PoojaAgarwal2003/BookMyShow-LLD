package com.bookmyshow.payment;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MockPaymentStrategyTest {
 private final MockPaymentStrategy strategy=new MockPaymentStrategy();
 @Test void approvesNormalAmount() {
  var result=strategy.pay(new BigDecimal("450.00"),"key-1");
  assertTrue(result.successful());
  assertNotNull(result.transactionId());
 }
 @Test void declinesAmountEndingInNinetyNineCents() {
  var result=strategy.pay(new BigDecimal("450.99"),"key-2");
  assertFalse(result.successful());
 }
}
