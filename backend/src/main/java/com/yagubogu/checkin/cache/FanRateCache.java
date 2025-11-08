package com.yagubogu.checkin.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class FanRateCache {

    private final Cache<LocalDate, List<GameWithFanRateParam>> cache;

    public FanRateCache() {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .build();
    }

    public void put(LocalDate date, List<GameWithFanRateParam> data) {
        cache.put(date, data);
    }

    public List<GameWithFanRateParam> get(LocalDate date) {
        return cache.getIfPresent(date);
    }

    public List<GameWithFanRateParam> getOrCompute(
            LocalDate date,
            Function<LocalDate, List<GameWithFanRateParam>> loader) {
        return cache.get(date, loader);
    }
}
