package com.bookmyshow.seat;

import com.bookmyshow.common.Enums.SeatStatus;
import com.bookmyshow.show.Show;
import com.bookmyshow.theatre.Seat;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "show_seats", uniqueConstraints = @UniqueConstraint(columnNames = { "show_id", "seat_id" }))
public class ShowSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Show show;
    @ManyToOne(optional = false)
    private Seat seat;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status = SeatStatus.AVAILABLE;
    @Version
    private Long version;
}
