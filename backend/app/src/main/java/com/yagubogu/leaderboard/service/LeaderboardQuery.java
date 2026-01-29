package com.yagubogu.leaderboard.service;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardItemResponse;
import java.util.List;

public interface LeaderboardQuery {

    LeaderboardType supports();

    List<LeaderboardItemResponse> findTop(int limit);
}
