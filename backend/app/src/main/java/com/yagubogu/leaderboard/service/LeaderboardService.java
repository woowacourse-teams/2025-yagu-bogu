package com.yagubogu.leaderboard.service;

import static java.util.Arrays.stream;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardItemResponse;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LeaderboardService {

    private final LeaderboardQueryRegistry registry;

    public List<LeaderboardResponse> findAllTop(int limit) {
        return stream(LeaderboardType.values())
                .map(type -> {
                    List<LeaderboardItemResponse> items = registry.getQuery(type)
                            .findTop(limit).stream()
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
