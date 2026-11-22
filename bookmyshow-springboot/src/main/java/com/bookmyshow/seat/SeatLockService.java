package com.bookmyshow.seat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.*;

@Service
public class SeatLockService {
  private final StringRedisTemplate redis;
  private final Duration ttl;
  private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
      "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);

  public SeatLockService(StringRedisTemplate redis,
      @Value("${app.booking.seat-lock-ttl-seconds:300}") long ttlSeconds) {
    this.redis = redis;
    this.ttl = Duration.ofSeconds(ttlSeconds);
  }

  public void lockSeats(Long showId, List<Long> seatIds, String owner) {
    List<Long> ids = seatIds.stream().distinct().sorted().toList();
    if (ids.size() != seatIds.size())
      throw new IllegalArgumentException("Duplicate seat IDs are not allowed");
    List<String> acquired = new ArrayList<>();
    try {
      for (Long id : ids) {
        String key = key(showId, id);
        Boolean ok = redis.opsForValue().setIfAbsent(key, owner, ttl);
        if (!Boolean.TRUE.equals(ok)) {
          if (owner.equals(redis.opsForValue().get(key))) {
            redis.expire(key, ttl);
            acquired.add(key);
            continue;
          }
          throw new IllegalStateException("One or more seats are already locked");
        }
        acquired.add(key);
      }
    } catch (RuntimeException ex) {
      for (String key : acquired)
        redis.execute(RELEASE_SCRIPT, List.of(key), owner);
      throw ex;
    }
  }

  public void assertOwnership(Long showId, List<Long> seatIds, String owner) {
    for (Long id : seatIds)
      if (!owner.equals(redis.opsForValue().get(key(showId, id))))
        throw new IllegalStateException("Seat lock expired or is not owned by this user");
  }

  public void releaseSeats(Long showId, List<Long> seatIds, String owner) {
    for (Long id : seatIds)
      redis.execute(RELEASE_SCRIPT, List.of(key(showId, id)), owner);
  }

  public boolean isLocked(Long showId, Long seatId) {
    return redis.opsForValue().get(key(showId, seatId)) != null;
  }

  public boolean isOwnedBy(Long showId, Long seatId, String owner) {
    if (owner == null)
      return false;
    return owner.equals(redis.opsForValue().get(key(showId, seatId)));
  }

  private String key(Long showId, Long seatId) {
    return "bms:show:" + showId + ":seat:" + seatId;
  }
}
