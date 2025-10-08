package com.yagubogu.stat.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import java.util.List;
import java.util.Optional;

public interface VictoryFairyRankingRepositoryCustom {

    List<VictoryFairyRank> findTopRankingByTeamFilterAndYear(TeamFilter teamFilter, int limit, int year);

    Optional<VictoryFairyRank> findByMemberAndTeamFilterAndYear(Member member, TeamFilter teamFilter, int year);
}
