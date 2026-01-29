package com.yagubogu.leaderboard.service;

import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardItemResponse;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class WinFairyLeaderboardQuery implements LeaderboardQuery {

    private final VictoryFairyRankingRepository victoryFairyRankingRepository;

    @Override
    public LeaderboardType supports() {
        return LeaderboardType.WIN_FAIRY;
    }

    @Override
    public List<LeaderboardItemResponse> findTop(final int limit) {
        int lastYear = Year.now().minusYears(1).getValue();
        List<VictoryFairyRankParam> victoryFairyRankings = victoryFairyRankingRepository.findTopRankingByTeamFilterAndYear(
                TeamFilter.ALL,
                limit,
                lastYear
        );

        return victoryFairyRankings.stream()
                .map(r -> new LeaderboardItemResponse(
                        Math.toIntExact(r.rank()),
                        r.memberId(),
                        r.nickname(),
                        r.teamShortName(),
                        r.profileImageUrl()
                ))
                .collect(Collectors.toList());
    }
}
