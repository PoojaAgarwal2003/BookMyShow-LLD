package com.bookmyshow.movie;

import com.bookmyshow.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByOrderByTitleAsc();
}
