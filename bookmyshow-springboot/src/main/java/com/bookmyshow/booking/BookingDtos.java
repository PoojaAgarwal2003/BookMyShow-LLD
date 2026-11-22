package com.bookmyshow.booking;

import jakarta.validation.constraints.*;
import java.util.List;

public final class BookingDtos {
    private BookingDtos() {
    }

    public record LockRequest(@NotNull Long showId, @NotEmpty List<@NotNull Long> showSeatIds) {
    }

    public record CreateBookingRequest(@NotNull Long showId, @NotEmpty List<@NotNull Long> showSeatIds,
            @NotBlank String idempotencyKey) {
    }

    public record BookingResponse(Long bookingId, String status, String totalAmount, List<Long> seatIds) {
    }
}
