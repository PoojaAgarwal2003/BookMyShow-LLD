package com.bookmyshow.show;

import com.bookmyshow.show.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ShowRepository extends JpaRepository<Show, Long> {
    List<Show> findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(Long movieId, java.time.LocalDateTime from);
}
