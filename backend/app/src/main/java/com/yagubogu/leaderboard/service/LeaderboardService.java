package com.yagubogu.leaderboard.service;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LeaderboardService {

    private final Map<LeaderboardType, LeaderboardQuery> map = new EnumMap<>(LeaderboardType.class);
    private final List<LeaderboardQuery> queries;

    @PostConstruct
    void init() {
        for (LeaderboardQuery query : queries) {
            map.put(query.supports(), query);
        }
    }

    public LeaderboardResponse findTop(LeaderboardType type, int limit) {
        LeaderboardQuery query = map.get(type);

        return LeaderboardResponse.of(type, query.findTop(limit));
    }
}
