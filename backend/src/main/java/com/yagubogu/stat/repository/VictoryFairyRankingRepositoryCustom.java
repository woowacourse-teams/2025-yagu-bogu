package com.yagubogu.stat.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.stadium.domain.VictoryFairyRanking;
import java.util.List;

public interface VictoryFairyRankingRepositoryCustom {

    List<VictoryFairyRanking> findTopByTeamFilter(TeamFilter teamFilter, int limit, int year);
}
