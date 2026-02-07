package com.yagubogu.leaderboard.service;

import static java.util.Arrays.stream;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardItemResponse;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
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

    public List<LeaderboardResponse> findAllTop(int limit) {
        return stream(LeaderboardType.values())
                .map(type -> {
                    LeaderboardQuery query = map.get(type);
                    if (query == null) {
                        throw new IllegalStateException("No LeaderboardQuery registered for type:" + type);
                    }
                    List<LeaderboardItemResponse> items = query.findTop(limit).stream()
                            .map(this::toItem)
                            .toList();
                    return LeaderboardResponse.of(type, items);
                })
                .toList();
    }

    private LeaderboardItemResponse toItem(LeaderboardRow row) {
        return new LeaderboardItemResponse(
                Math.toIntExact(row.rank()),
                row.memberId(),
                row.nickname(),
                row.favoriteTeam(),
                row.profileImageUrl(),
                row.score()
        );
    }
}
