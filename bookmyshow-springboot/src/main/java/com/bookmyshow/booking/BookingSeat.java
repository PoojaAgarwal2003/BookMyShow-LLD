package com.bookmyshow.booking;

import com.bookmyshow.seat.ShowSeat;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "booking_seats")
public class BookingSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Booking booking;
    @ManyToOne(optional = false)
    private ShowSeat showSeat;
}
