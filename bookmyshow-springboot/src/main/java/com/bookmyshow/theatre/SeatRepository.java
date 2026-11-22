package com.bookmyshow.theatre;

import com.bookmyshow.theatre.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenIdOrderByRowLabelAscSeatNumberAsc(Long screenId);
}
