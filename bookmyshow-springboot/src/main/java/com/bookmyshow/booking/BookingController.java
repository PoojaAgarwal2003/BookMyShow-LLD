package com.bookmyshow.booking;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping("/lock")
    public void lock(@Valid @RequestBody BookingDtos.LockRequest r, Authentication a) {
        service.lock(r, a.getName());
    }

    @PostMapping
    public BookingDtos.BookingResponse create(@Valid @RequestBody BookingDtos.CreateBookingRequest r,
            Authentication a) {
        return service.create(r, a.getName());
    }

    @GetMapping("/{id}")
    public BookingDtos.BookingResponse get(@PathVariable Long id, Authentication a) {
        return service.get(id, a.getName());
    }
}
