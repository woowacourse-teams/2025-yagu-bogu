package com.yagubogu.leaderboard.service;

import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class LoseFairyLeaderboardQuery implements LeaderboardQuery {

    private final VictoryFairyRankingRepository victoryFairyRankingRepository;

    @Override
    public LeaderboardType supports() {
        return LeaderboardType.LOSE_FAIRY;
    }

    @Override
    public List<LeaderboardRow> findTop(final int limit) {
        int lastYear = Year.now().minusYears(1).getValue();
        List<VictoryFairyRankParam> victoryFairyRankings = victoryFairyRankingRepository.findBottomRankingByTeamFilterAndYear(
                TeamFilter.ALL,
                limit,
                lastYear
        );

        return victoryFairyRankings.stream()
                .map(r -> new LeaderboardRow(
                        r.rank(),
                        r.memberId(),
                        r.nickname(),
                        r.teamShortName(),
                        r.profileImageUrl(),
                        r.score()
                ))
                .collect(Collectors.toList());
    }
}
