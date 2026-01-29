package com.yagubogu.leaderboard.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MostCheckInLeaderboardQuery implements LeaderboardQuery {

    private final CheckInRepository checkInRepository;

    @Override
    public LeaderboardType supports() {
        return LeaderboardType.MOST_CHECK_IN;
    }

    @Override
    public List<LeaderboardRow> findTop(final int limit) {
        return checkInRepository.findMostCheckInWinner(limit);
    }
}
