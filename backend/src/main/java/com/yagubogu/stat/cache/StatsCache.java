package com.yagubogu.stat.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class StatsCache {

    private static final String STADIUM_TOTAL_FORMAT = "stats:stadium:%d:total";
    private static final String STADIUM_TEAM_FORMAT = "stats:stadium:%d:team:%d";

    private final Cache<String, AtomicLong> cache;

    public StatsCache() {
        this.cache = Caffeine.newBuilder().build();
    }

    public long incrementStadiumTotal(final Long stadiumId) {
        return incrementAndGet(stadiumTotalKey(stadiumId));
    }

    public long incrementTeamCount(final Long stadiumId, final Long teamId) {
        return incrementAndGet(stadiumTeamKey(stadiumId, teamId));
    }

    public void overwriteAll(final Map<String, Long> values) {
        cache.invalidateAll();
        values.forEach((key, value) -> cache.put(key, new AtomicLong(value)));
    }

    public static String stadiumTotalKey(final Long stadiumId) {
        return STADIUM_TOTAL_FORMAT.formatted(stadiumId);
    }

    public static String stadiumTeamKey(final Long stadiumId, final Long teamId) {
        return STADIUM_TEAM_FORMAT.formatted(stadiumId, teamId);
    }

    private long incrementAndGet(final String key) {
        return cache.get(key, unused -> new AtomicLong(0L)).incrementAndGet();
    }
}
