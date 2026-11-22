# Architecture notes

## Booking lifecycle

`AVAILABLE inventory + Redis lock -> PENDING booking -> payment -> CONFIRMED/FAILED`

- Redis lock key: `bms:show:{showId}:seat:{showSeatId}`
- Lock value: authenticated user's email (owner token)
- TTL: 300 seconds by default
- Release uses Lua compare-and-delete so only the owner can release a lock.
- PostgreSQL `show_seats` rows are locked with `PESSIMISTIC_WRITE` before final state transition.
- `booking_seats.show_seat_id` is unique to prevent two bookings from owning the same show-seat row.

## Main relationships

- City 1:N Theatre
- Theatre 1:N Screen
- Screen 1:N Seat
- Movie 1:N Show
- Screen 1:N Show
- Show 1:N ShowSeat
- Seat 1:N ShowSeat (one inventory row per show)
- User 1:N Booking
- Booking 1:N BookingSeat
- Booking 1:N Payment

## Key consistency rule

Redis is a temporary coordination layer, not the source of truth. PostgreSQL constraints and row locks protect final inventory transitions.
