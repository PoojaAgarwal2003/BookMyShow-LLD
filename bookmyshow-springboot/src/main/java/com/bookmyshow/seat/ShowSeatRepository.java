package com.bookmyshow.seat;

import com.bookmyshow.common.Enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {
    List<ShowSeat> findByShowIdOrderBySeatRowLabelAscSeatSeatNumberAsc(Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ss from ShowSeat ss where ss.show.id=:showId and ss.id in :ids order by ss.id")
    List<ShowSeat> findForUpdate(@Param("showId") Long showId, @Param("ids") Collection<Long> ids);
}
