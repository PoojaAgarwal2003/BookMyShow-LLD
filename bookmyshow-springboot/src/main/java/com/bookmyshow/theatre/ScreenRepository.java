package com.bookmyshow.theatre;

import com.bookmyshow.theatre.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByTheatreId(Long theatreId);
}
