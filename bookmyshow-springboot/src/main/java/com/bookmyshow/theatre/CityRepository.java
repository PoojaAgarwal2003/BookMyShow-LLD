package com.bookmyshow.theatre;

import com.bookmyshow.theatre.City;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CityRepository extends JpaRepository<City, Long> {
    Optional<City> findByNameIgnoreCase(String name);
}
