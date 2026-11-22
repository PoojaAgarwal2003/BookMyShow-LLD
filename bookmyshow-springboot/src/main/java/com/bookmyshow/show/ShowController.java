package com.bookmyshow.show;

import com.bookmyshow.seat.*;
import com.bookmyshow.common.Enums.SeatStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {
    private final ShowRepository shows;
    private final ShowSeatRepository seats;
    private final SeatLockService locks;

    public ShowController(ShowRepository s, ShowSeatRepository ss, SeatLockService l) {
        shows = s;
        seats = ss;
        locks = l;
    }

    @GetMapping
    public List<Show> byMovie(@RequestParam Long movieId) {
        return shows.findByMovieIdAndStartTimeAfterOrderByStartTimeAsc(movieId, LocalDateTime.now());
    }

    @GetMapping("/{id}/seats")
    public List<ShowSeat> seats(@PathVariable Long id, Authentication authentication) {
        List<ShowSeat> inventory = seats.findByShowIdOrderBySeatRowLabelAscSeatSeatNumberAsc(id);
        String currentUser = authentication == null ? null : authentication.getName();

        for (ShowSeat showSeat : inventory) {
            if (showSeat.getStatus() == SeatStatus.AVAILABLE &&
                    locks.isLocked(id, showSeat.getId()) &&
                    !locks.isOwnedBy(id, showSeat.getId(), currentUser)) {
                showSeat.setStatus(SeatStatus.LOCKED);
            }
        }
        return inventory;
    }
}
