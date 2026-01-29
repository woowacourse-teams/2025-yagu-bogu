package com.yagubogu.leaderboard.service;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import java.util.List;

public interface LeaderboardQuery {

    LeaderboardType supports();

    List<LeaderboardRow> findTop(int limit);
}
