package com.yagubogu.leaderboard.service;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LeaderboardQueryRegistry {

    private final List<LeaderboardQuery> queries;
    private Map<LeaderboardType, LeaderboardQuery> map;

    @PostConstruct
    void init() {
        map = new EnumMap<>(LeaderboardType.class);
        for (LeaderboardQuery query : queries) {
            map.put(query.supports(), query);
        }
    }

    public LeaderboardQuery getQuery(LeaderboardType type) {
        LeaderboardQuery query = map.get(type);
        if (query == null) {
            throw new IllegalStateException("No LeaderboardQuery registered for type: " + type);
        }
        return query;
    }
}
