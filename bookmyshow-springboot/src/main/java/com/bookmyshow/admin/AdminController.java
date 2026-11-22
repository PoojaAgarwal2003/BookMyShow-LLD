package com.bookmyshow.admin;

import com.bookmyshow.common.Enums.SeatType;
import com.bookmyshow.movie.*;
import com.bookmyshow.seat.*;
import com.bookmyshow.show.*;
import com.bookmyshow.theatre.*;
import com.bookmyshow.user.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserRepository users;
    private final MovieRepository movies;
    private final TheatreRepository theatres;
    private final ScreenRepository screens;
    private final SeatRepository seats;
    private final ShowRepository shows;
    private final ShowSeatRepository showSeats;

    public AdminController(UserRepository users, MovieRepository movies, TheatreRepository theatres,
            ScreenRepository screens, SeatRepository seats, ShowRepository shows,
            ShowSeatRepository showSeats) {
        this.users = users;
        this.movies = movies;
        this.theatres = theatres;
        this.screens = screens;
        this.seats = seats;
        this.shows = shows;
        this.showSeats = showSeats;
    }

    public record MovieRequest(String title, Integer durationMinutes, String language, String genre, String synopsis) {}
    public record ScreenRequest(Long theatreId, String name, Integer rows, Integer seatsPerRow,
            BigDecimal regularPrice, BigDecimal premiumPrice) {}
    public record ShowRequest(Long movieId, Long screenId, LocalDateTime startTime, BigDecimal regularPrice,
            BigDecimal premiumPrice) {}

    private void requireAdmin(Authentication auth) {
        if (auth == null)
            throw new IllegalStateException("Authentication required");
        User user = users.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Admin account not found"));
        if (!user.isAdmin())
            throw new IllegalStateException("Admin access required");
    }

    @PostMapping("/movies")
    public Movie addMovie(@RequestBody @Valid MovieRequest request, Authentication auth) {
        requireAdmin(auth);
        if (request.title() == null || request.title().isBlank())
            throw new IllegalArgumentException("Movie title is required");
        Movie movie = new Movie();
        movie.setTitle(request.title().trim());
        movie.setDurationMinutes(request.durationMinutes() == null ? 120 : request.durationMinutes());
        movie.setLanguage(request.language());
        movie.setGenre(request.genre());
        movie.setSynopsis(request.synopsis());
        return movies.save(movie);
    }

    @PostMapping("/screens")
    public Screen addScreen(@RequestBody @Valid ScreenRequest request, Authentication auth) {
        requireAdmin(auth);
        Theatre theatre = theatres.findById(request.theatreId())
                .orElseThrow(() -> new IllegalArgumentException("Theatre not found"));
        Screen screen = new Screen();
        screen.setName(request.name());
        screen.setTheatre(theatre);
        screen = screens.save(screen);

        int rowCount = request.rows() == null ? 6 : request.rows();
        int seatsPerRow = request.seatsPerRow() == null ? 8 : request.seatsPerRow();
        if (rowCount < 1 || seatsPerRow < 1)
            throw new IllegalArgumentException("Rows and seats per row must be positive");

        for (int row = 0; row < rowCount; row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int number = 1; number <= seatsPerRow; number++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(number);
                seat.setType(row >= rowCount - 2 ? SeatType.PREMIUM : SeatType.REGULAR);
                seats.save(seat);
            }
        }
        return screen;
    }

    @PostMapping("/shows")
    public Show addShow(@RequestBody @Valid ShowRequest request, Authentication auth) {
        requireAdmin(auth);
        Movie movie = movies.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        Screen screen = screens.findById(request.screenId())
                .orElseThrow(() -> new IllegalArgumentException("Screen not found"));
        if (request.startTime() == null)
            throw new IllegalArgumentException("Start time is required");

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(request.startTime());
        show.setEndTime(request.startTime().plusMinutes(movie.getDurationMinutes()));
        show = shows.save(show);

        BigDecimal regularPrice = request.regularPrice() == null ? new BigDecimal("250.00") : request.regularPrice();
        BigDecimal premiumPrice = request.premiumPrice() == null ? new BigDecimal("350.00") : request.premiumPrice();
        for (Seat seat : seats.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screen.getId())) {
            ShowSeat inventory = new ShowSeat();
            inventory.setShow(show);
            inventory.setSeat(seat);
            inventory.setPrice(seat.getType() == SeatType.PREMIUM ? premiumPrice : regularPrice);
            showSeats.save(inventory);
        }
        return show;
    }

    @GetMapping("/catalog")
    public Map<String, Object> catalog(Authentication auth) {
        requireAdmin(auth);
        Map<String, Object> catalog = new LinkedHashMap<>();
        catalog.put("movies", movies.findAllByOrderByTitleAsc());
        catalog.put("theatres", theatres.findAll());
        catalog.put("screens", screens.findAll());
        catalog.put("shows", shows.findAll());
        return catalog;
    }
}
