package yagubogu.crawling.game.service.poller;

import com.yagubogu.game.domain.Game;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameScheduleManager {

    private static final LocalTime DEFAULT_GAME_START_TIME = LocalTime.of(18, 30);
    private static final Duration POLLING_INTERVAL = Duration.ofMinutes(1);

    private final Clock clock;
    private final Map<Long, Instant> nextPollTime = new ConcurrentHashMap<>();
    private volatile Instant globalWakeTime = Instant.MAX;

    public GameScheduleManager(Clock clock) {
        this.clock = clock;
    }

    /**
     * 오늘 경기 스케줄 초기화
     *
     * 각 경기의 첫 폴링 시각 계산:
     * - 경기 시작 전: 시작 시각에 확인
     * - 경기 시작 후: 현재 시각 + 1분 후 확인
     */
    public void initialize(List<Game> games, LocalDate today) {
        nextPollTime.clear();

        if (games.isEmpty()) {
            globalWakeTime = calculateTomorrow(today);
            log.info("[SCHEDULE] No games today. Sleep until tomorrow.");
            return;
        }

        Instant now = Instant.now(clock);
        Instant earliestPollTime = null;

        for (Game game : games) {
            if (game.getGameState().isFinalized()) {
                continue;
            }

            Instant gameStartTime = calculateGameStartTime(game);
            Instant pollTime = calculateInitialPollTime(gameStartTime, now);

            nextPollTime.put(game.getId(), pollTime);
            earliestPollTime = earliestOf(earliestPollTime, pollTime);
        }

        globalWakeTime = Optional.ofNullable(earliestPollTime)
                .filter(t -> t.isAfter(now))
                .orElseGet(() -> {
                    log.info("[SCHEDULE] All games finalized. Sleep until tomorrow.");
                    return calculateTomorrow(today);
                });

        log.info("[SCHEDULE] Initialized {} games. Next wake: {}",
                nextPollTime.size(),
                globalWakeTime.atZone(clock.getZone()));
    }

    public boolean shouldWake(Instant now) {
        return !now.isBefore(globalWakeTime);
    }

    public boolean shouldPollGame(Long gameId, Instant now) {
        Instant pollTime = nextPollTime.get(gameId);
        return pollTime != null && !now.isBefore(pollTime);
    }

    public void scheduleNextPoll(Long gameId) {
        Instant nextTime = Instant.now(clock).plus(POLLING_INTERVAL);
        nextPollTime.put(gameId, nextTime);
    }

    public void scheduleNextPollAt(Long gameId, Instant nextTime) {
        if (nextTime != null) {
            nextPollTime.put(gameId, nextTime);
        }
    }

    public void removeGame(Long gameId) {
        nextPollTime.remove(gameId);
    }

    public void clearAll() {
        nextPollTime.clear();
    }

    public void sleepUntilTomorrow(LocalDate today) {
        globalWakeTime = calculateTomorrow(today);
    }

    /**
     * 초기 폴링 시각 계산
     * - 경기 시작 전: 시작 시각
     * - 경기 시작 후: 현재 + 1분
     */
    private Instant calculateInitialPollTime(Instant gameStartTime, Instant now) {
        if (now.isBefore(gameStartTime)) {
            return gameStartTime;
        }
        return now.plus(POLLING_INTERVAL);
    }

    private Instant calculateGameStartTime(Game game) {
        LocalTime startTime = Objects.requireNonNullElse(
                game.getStartAt(),
                DEFAULT_GAME_START_TIME
        );

        return ZonedDateTime.of(game.getDate(), startTime, clock.getZone())
                .toInstant();
    }

    private Instant calculateTomorrow(LocalDate today) {
        return ZonedDateTime.of(
                today.plusDays(1),
                LocalTime.MIDNIGHT,
                clock.getZone()
        ).toInstant();
    }

    private static Instant earliestOf(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }
}
