package com.bookmyshow.theatre;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class CatalogController {
    private final CityRepository cities;
    private final TheatreRepository theatres;
    private final ScreenRepository screens;
    private final SeatRepository seats;

    public CatalogController(CityRepository c, TheatreRepository t, ScreenRepository s, SeatRepository se) {
        cities = c;
        theatres = t;
        screens = s;
        seats = se;
    }

    @GetMapping("/api/v1/cities")
    public List<City> cities() {
        return cities.findAll();
    }

    @GetMapping("/api/v1/theatres")
    public List<Theatre> theatres(@RequestParam Long cityId) {
        return theatres.findByCityId(cityId);
    }

    @GetMapping("/api/v1/theatres/{theatreId}/screens")
    public List<Screen> screens(@PathVariable Long theatreId) {
        return screens.findByTheatreId(theatreId);
    }

    @GetMapping("/api/v1/screens/{screenId}/seats")
    public List<Seat> seats(@PathVariable Long screenId) {
        return seats.findByScreenIdOrderByRowLabelAscSeatNumberAsc(screenId);
    }
}
