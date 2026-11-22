package com.bookmyshow.payment;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    public record PayRequest(String method) {
    }

    @PostMapping("/booking/{bookingId}")
    public Payment pay(@PathVariable Long bookingId, @RequestBody PayRequest req, Authentication auth) {
        return service.pay(bookingId, auth.getName(), req.method() == null ? "MOCK" : req.method());
    }
}
