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
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.config.CrawlerSchedulerProperties;

@Slf4j
@Component
public class GameScheduleManager {

    private final Clock clock;
    private final CrawlerSchedulerProperties props;
    private final Map<Long, Instant> nextPoll = new ConcurrentHashMap<>();
    private volatile Instant globalWake = Instant.MAX;

    public GameScheduleManager(Clock clock, final CrawlerSchedulerProperties props) {
        this.clock = clock;
        this.props = props;
    }

    public void initialize(List<Game> games, LocalDate today) {
        nextPoll.clear();

        if (games.isEmpty()) {
            globalWake = getTomorrow(today);
            log.info("[SCHEDULE] no games. sleep until {}", globalWake.atZone(clock.getZone()));
            return;
        }

        Instant now = Instant.now(clock);
        Instant earliest = null;
        for (Game game : games) {
            if (game.getGameState().isFinalized()) {
                continue;
            }
            Instant start = getStartTime(game);
            Instant pollAt = calculateInitialPollTime(start, now);
            nextPoll.put(game.getId(), pollAt);
            earliest = calculateEarliestTime(earliest, pollAt);
        }
        globalWake = calculateGlobalWake(earliest, now);
        log.info("[SCHEDULE] Initialized {} games. Next wake: {}", nextPoll.size(), globalWake.atZone(clock.getZone()));
    }

    public boolean shouldWake(Instant now) {
        if (globalWake == Instant.MAX) {
            log.debug("[SCHEDULE] Not initialized yet, skipping poll");
            return false;
        }
        boolean shouldWake = !now.isBefore(globalWake);
        log.debug("[SCHEDULE] shouldWake={}, now={}, globalWakeTime={}, diff={}m[in",
                shouldWake,
                now.atZone(clock.getZone()),
                globalWake.atZone(clock.getZone()),
                Duration.between(now, globalWake).toMinutes());
        return shouldWake;
    }

    public boolean shouldPollGame(Long gameId, Instant now) {
        Instant pollTime = nextPoll.get(gameId);
        return pollTime != null && !now.isBefore(pollTime);
    }

    public void scheduleNextPoll(Long gameId) {
        Instant nextTime = Instant.now(clock).plus(props.getPollingInterval());
        nextPoll.put(gameId, nextTime);
    }

    public void scheduleNextPollAt(Long gameId, Instant nextTime) {
        if (nextTime != null) {
            nextPoll.put(gameId, nextTime);
        }
    }

    public void removeGame(Long gameId) {
        nextPoll.remove(gameId);
    }

    public void clearAll() {
        nextPoll.clear();
    }

    public void sleepUntilTomorrow(LocalDate today) {
        globalWake = getTomorrow(today);
    }

    private Instant calculateInitialPollTime(Instant gameStartTime, Instant now) {
        if (now.isBefore(gameStartTime)) {
            return gameStartTime;
        }
        return now.plus(props.getPollingInterval());
    }

    private Instant getTomorrow(LocalDate today) {
        return ZonedDateTime.of(
                today.plusDays(1),
                LocalTime.MIDNIGHT,
                clock.getZone()
        ).toInstant();
    }

    private Instant calculateGlobalWake(final Instant earliest, final Instant now) {
        if (earliest != null && earliest.isAfter(now)) {
            return earliest;
        }
        return now;
    }

    private Instant getStartTime(Game game) {
        LocalTime startTime = Objects.requireNonNullElse(
                game.getStartAt(),
                props.getDefaultGameStartTime()
        );

        return ZonedDateTime.of(game.getDate(), startTime, clock.getZone())
                .toInstant();
    }

    private static Instant calculateEarliestTime(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }
}
