package com.bookmyshow.payment;

import com.bookmyshow.booking.*;
import com.bookmyshow.common.Enums.*;
import com.bookmyshow.seat.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    private final BookingRepository bookings;
    private final BookingSeatRepository bookingSeats;
    private final ShowSeatRepository showSeats;
    private final PaymentRepository payments;
    private final SeatLockService locks;
    private final PaymentFactory factory;

    public PaymentService(BookingRepository b, BookingSeatRepository bs, ShowSeatRepository ss,
            PaymentRepository p, SeatLockService l, PaymentFactory f) {
        bookings = b;
        bookingSeats = bs;
        showSeats = ss;
        payments = p;
        locks = l;
        factory = f;
    }

    @Transactional
    public Payment pay(Long bookingId, String owner, String method) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() != BookingStatus.PENDING)
            throw new IllegalStateException("Booking is not pending");
        List<Long> ids = bookingSeats.findByBookingId(bookingId).stream().map(x -> x.getShowSeat().getId()).toList();
        locks.assertOwnership(booking.getShow().getId(), ids, owner);
        PaymentStrategy.PaymentOutcome outcome = factory.get(method).pay(booking.getTotalAmount(),
                "booking-" + bookingId);
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setStatus(outcome.successful() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setTransactionId(outcome.transactionId());
        payment.setCreatedAt(LocalDateTime.now());
        payments.save(payment);
        if (outcome.successful()) {
            var locked = showSeats.findForUpdate(booking.getShow().getId(), ids);
            if (locked.size() != ids.size() || locked.stream().anyMatch(s -> s.getStatus() != SeatStatus.AVAILABLE))
                throw new IllegalStateException("One or more seats are no longer available");
            locked.forEach(s -> s.setStatus(SeatStatus.BOOKED));
            booking.setStatus(BookingStatus.CONFIRMED);
        } else {
            booking.setStatus(BookingStatus.FAILED);
            showSeats.findForUpdate(booking.getShow().getId(), ids).forEach(s -> s.setStatus(SeatStatus.AVAILABLE));
        }
        bookings.save(booking);
        locks.releaseSeats(booking.getShow().getId(), ids, owner);
        return payment;
    }
}
