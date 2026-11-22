package com.bookmyshow.common;

public final class Enums {
    private Enums() {
    }

    public enum SeatStatus {
        AVAILABLE, LOCKED, BOOKED
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, FAILED, CANCELLED, EXPIRED
    }

    public enum PaymentStatus {
        INITIATED, SUCCESS, FAILED, REFUNDED
    }

    public enum SeatType {
        REGULAR, PREMIUM, RECLINER
    }
}
