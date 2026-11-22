package com.bookmyshow.booking;

import com.bookmyshow.common.Enums.BookingStatus;
import com.bookmyshow.seat.SeatLockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Marks stale pending bookings expired. Seat locks have independent Redis TTLs.
 * A production deployment should also reconcile payment callbacks and release
 * seats
 * with a durable outbox/job rather than relying only on this periodic sweep.
 */
@Component
public class BookingExpiryJob {
  private final BookingRepository bookings;
  private final BookingSeatRepository bookingSeats;
  private final SeatLockService locks;

  public BookingExpiryJob(BookingRepository bookings, BookingSeatRepository bookingSeats,
          SeatLockService locks) {
      this.bookings = bookings;
      this.bookingSeats = bookingSeats;
      this.locks = locks;
  }

  @Scheduled(fixedDelayString = "${app.booking.expiry-sweep-ms:60000}")
  @Transactional
  public void expireStaleBookings() {
      LocalDateTime expiryThreshold = LocalDateTime.now().minusMinutes(6);

      bookings.findAll().stream()
              .filter(b -> b.getStatus() == BookingStatus.PENDING
                      && b.getCreatedAt().isBefore(expiryThreshold))
              .forEach(booking -> {
                  java.util.List<Long> seatIds = bookingSeats.findByBookingId(booking.getId()).stream()
                          .map(link -> link.getShowSeat().getId())
                          .toList();
                  locks.releaseSeats(booking.getShow().getId(), seatIds,
                          booking.getUser().getEmail());
                  booking.setStatus(BookingStatus.EXPIRED);
              });
  }
}
