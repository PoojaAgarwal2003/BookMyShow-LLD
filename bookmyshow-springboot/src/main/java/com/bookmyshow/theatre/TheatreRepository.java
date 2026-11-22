package com.bookmyshow.theatre;

import com.bookmyshow.theatre.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByCityId(Long cityId);
}
