package com.bookmyshow;

import com.bookmyshow.movie.*;
import com.bookmyshow.show.*;
import com.bookmyshow.seat.*;
import com.bookmyshow.theatre.*;
import com.bookmyshow.common.Enums.SeatType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Creates a rich local demo catalog on first startup and fills missing demo data on later starts. */
@Configuration
public class DemoDataInitializer {
    @Bean
    CommandLineRunner seedDemoData(MovieRepository movies, CityRepository cities,
            TheatreRepository theatres, ScreenRepository screens, SeatRepository seats,
            ShowRepository shows, ShowSeatRepository showSeats,
            com.bookmyshow.user.UserRepository users,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            com.bookmyshow.user.User admin = users.findByEmail("admin").orElseGet(() ->
                    new com.bookmyshow.user.User("Administrator", "admin", passwordEncoder.encode("admin")));
            admin.setName("Administrator");
            admin.setPasswordHash(passwordEncoder.encode("admin"));
            admin.setAdmin(true);
            users.save(admin);

            City gurugram = cities.findByNameIgnoreCase("Gurugram")
                    .orElseGet(() -> {
                        City city = new City();
                        city.setName("Gurugram");
                        return cities.save(city);
                    });

            Theatre bms = createTheatre(theatres, gurugram, "BMS Cinemas: Sector 29", "Sector 29, Gurugram");
            Theatre inox = createTheatre(theatres, gurugram, "INOX: MGF Metropolitan", "MGF Metropolitan Mall");

            Screen screen1 = createScreen(screens, bms, "Screen 1");
            Screen screen2 = createScreen(screens, bms, "Screen 2");
            Screen screen3 = createScreen(screens, inox, "Audi 1");

            createSeats(seats, screen1, 10, 6);
            createSeats(seats, screen2, 8, 6);
            createSeats(seats, screen3, 10, 8);

            Movie adventure = createMovie(movies, "The Sample Adventure", 130, "Hindi", "Adventure",
                    "A demo adventure about friendship, courage and the journey home.");
            Movie midnight = createMovie(movies, "Midnight Express", 118, "English", "Thriller",
                    "A tense overnight journey becomes a fight for survival.");
            Movie monsoon = createMovie(movies, "Monsoon Hearts", 142, "Hindi", "Romance",
                    "Two strangers find love during one unforgettable monsoon.");
            Movie galaxy = createMovie(movies, "Galaxy Beyond", 156, "English", "Sci-Fi",
                    "A team crosses beyond the known galaxy to answer a mysterious signal.");
            Movie laugh = createMovie(movies, "The Last Laugh", 124, "Hindi", "Comedy",
                    "One chaotic family, one wedding and a comedy of errors.");
            Movie legend = createMovie(movies, "The Iron Legend", 148, "Tamil", "Action",
                    "An ordinary guardian rises against an empire of corruption.");

            createShowWithInventory(shows, showSeats, seats, midnight, screen1, 1, 10, 30);
            createShowWithInventory(shows, showSeats, seats, midnight, screen1, 1, 14, 30);
            createShowWithInventory(shows, showSeats, seats, monsoon, screen2, 1, 11, 15);
            createShowWithInventory(shows, showSeats, seats, monsoon, screen2, 1, 16, 0);
            createShowWithInventory(shows, showSeats, seats, galaxy, screen3, 1, 12, 0);
            createShowWithInventory(shows, showSeats, seats, galaxy, screen3, 1, 19, 45);
            createShowWithInventory(shows, showSeats, seats, laugh, screen1, 2, 13, 0);
            createShowWithInventory(shows, showSeats, seats, legend, screen2, 2, 18, 30);
            createShowWithInventory(shows, showSeats, seats, adventure, screen3, 1, 18, 30);
        };
    }

    private Theatre createTheatre(TheatreRepository theatres, City city, String name, String address) {
        return theatres.findByCityId(city.getId()).stream()
                .filter(theatre -> theatre.getName().equals(name)).findFirst()
                .orElseGet(() -> {
                    Theatre theatre = new Theatre();
                    theatre.setName(name);
                    theatre.setAddress(address);
                    theatre.setCity(city);
                    return theatres.save(theatre);
                });
    }

    private Screen createScreen(ScreenRepository screens, Theatre theatre, String name) {
        return screens.findByTheatreId(theatre.getId()).stream()
                .filter(screen -> screen.getName().equals(name)).findFirst()
                .orElseGet(() -> {
                    Screen screen = new Screen();
                    screen.setName(name);
                    screen.setTheatre(theatre);
                    return screens.save(screen);
                });
    }

    private void createSeats(SeatRepository seats, Screen screen, int rows, int seatsPerRow) {
        if (!seats.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screen.getId()).isEmpty())
            return;

        for (int row = 0; row < rows; row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int number = 1; number <= seatsPerRow; number++) {
                Seat seat = new Seat();
                seat.setScreen(screen);
                seat.setRowLabel(rowLabel);
                seat.setSeatNumber(number);
                seat.setType(row >= rows - 2 ? SeatType.PREMIUM : SeatType.REGULAR);
                seats.save(seat);
            }
        }
    }

    private Movie createMovie(MovieRepository movies, String title, int duration, String language,
            String genre, String synopsis) {
        return movies.findAllByOrderByTitleAsc().stream()
                .filter(movie -> movie.getTitle().equals(title)).findFirst()
                .orElseGet(() -> {
                    Movie movie = new Movie();
                    movie.setTitle(title);
                    movie.setDurationMinutes(duration);
                    movie.setLanguage(language);
                    movie.setGenre(genre);
                    movie.setSynopsis(synopsis);
                    return movies.save(movie);
                });
    }

    private void createShowWithInventory(ShowRepository shows, ShowSeatRepository showSeats,
            SeatRepository seats, Movie movie, Screen screen, int daysFromNow, int hour, int minute) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(daysFromNow)
                .withHour(hour).withMinute(minute).withSecond(0).withNano(0);

        boolean alreadyExists = shows.findAll().stream().anyMatch(show ->
                show.getMovie().getId().equals(movie.getId())
                        && show.getScreen().getId().equals(screen.getId())
                        && show.getStartTime().equals(startTime));
        if (alreadyExists)
            return;

        Show show = new Show();
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(startTime);
        show.setEndTime(startTime.plusMinutes(movie.getDurationMinutes()));
        show = shows.save(show);

        for (Seat seat : seats.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screen.getId())) {
            ShowSeat inventory = new ShowSeat();
            inventory.setShow(show);
            inventory.setSeat(seat);
            inventory.setPrice(seat.getType() == SeatType.PREMIUM
                    ? new BigDecimal("350.00")
                    : new BigDecimal("250.00"));
            showSeats.save(inventory);
        }
    }
}
