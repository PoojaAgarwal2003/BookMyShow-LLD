package com.bookmyshow.theatre;

import com.bookmyshow.common.Enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "screen_id", "row_label", "seat_number" }))
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Screen screen;
    @Column(name = "row_label", nullable = false)
    private String rowLabel;
    @Column(nullable = false)
    private Integer seatNumber;
    @Enumerated(EnumType.STRING)
    private SeatType type = SeatType.REGULAR;
}
