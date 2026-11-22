package com.bookmyshow.booking;

import com.bookmyshow.common.Enums.*;
import com.bookmyshow.seat.*;
import com.bookmyshow.show.ShowRepository;
import com.bookmyshow.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BookingService {
  private final BookingRepository bookings;
  private final BookingSeatRepository bookingSeats;
  private final ShowSeatRepository showSeats;
  private final ShowRepository shows;
  private final UserRepository users;
  private final SeatLockService locks;

  public BookingService(BookingRepository b, BookingSeatRepository bs, ShowSeatRepository ss,
      ShowRepository sr, UserRepository ur, SeatLockService l) {
    bookings = b;
    bookingSeats = bs;
    showSeats = ss;
    shows = sr;
    users = ur;
    locks = l;
  }

  public void lock(BookingDtos.LockRequest r, String owner) {
    var found = showSeats.findAllById(r.showSeatIds());
    if (found.size() != r.showSeatIds().stream().distinct().count() ||
        found.stream().anyMatch(s -> !s.getShow().getId().equals(r.showId()) || s.getStatus() != SeatStatus.AVAILABLE))
      throw new IllegalStateException("Selected seats are not available for this show");
    locks.lockSeats(r.showId(), r.showSeatIds(), owner);
  }

  @Transactional
  public BookingDtos.BookingResponse create(BookingDtos.CreateBookingRequest r, String email) {
    var existing = bookings.findByIdempotencyKey(r.idempotencyKey());
    if (existing.isPresent())
      return response(existing.get());
    var user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    var show = shows.findById(r.showId()).orElseThrow(() -> new IllegalArgumentException("Show not found"));
    List<Long> ids = r.showSeatIds().stream().distinct().sorted().toList();
    if (ids.size() != r.showSeatIds().size())
      throw new IllegalArgumentException("Duplicate seat IDs");
    locks.assertOwnership(r.showId(), ids, email);
    var selected = showSeats.findForUpdate(r.showId(), ids);
    if (selected.size() != ids.size() || selected.stream().anyMatch(s -> s.getStatus() != SeatStatus.AVAILABLE))
      throw new IllegalStateException("Selected seats are no longer available");
    Booking booking = new Booking();
    booking.setUser(user);
    booking.setShow(show);
    booking.setTotalAmount(selected.stream().map(ShowSeat::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
    booking.setStatus(BookingStatus.PENDING);
    booking.setIdempotencyKey(r.idempotencyKey());
    booking.setCreatedAt(LocalDateTime.now());
    booking = bookings.save(booking);
    for (var ss : selected) {
      BookingSeat bs = new BookingSeat();
      bs.setBooking(booking);
      bs.setShowSeat(ss);
      bookingSeats.save(bs);
    }
    return response(booking);
  }

  @Transactional(readOnly = true)
  public BookingDtos.BookingResponse get(Long id, String email) {
    Booking b = bookings.findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    if (!b.getUser().getEmail().equals(email))
      throw new IllegalArgumentException("Booking not found");
    return response(b);
  }

  private BookingDtos.BookingResponse response(Booking b) {
    return new BookingDtos.BookingResponse(b.getId(), b.getStatus().name(), b.getTotalAmount().toPlainString(),
        bookingSeats.findByBookingId(b.getId()).stream().map(x -> x.getShowSeat().getId()).toList());
  }
}
