package com.bookmyshow.movie;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {
    private final MovieRepository repo;

    public MovieController(MovieRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Movie> all() {
        return repo.findAllByOrderByTitleAsc();
    }

    @GetMapping("/{id}")
    public Movie get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Movie not found"));
    }
}
