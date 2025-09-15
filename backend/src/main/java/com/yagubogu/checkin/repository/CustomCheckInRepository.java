package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import java.util.List;

public interface CustomCheckInRepository {

    double calculateTotalAverageWinRate(int year);

    double calculateAverageCheckInCount(int year);

    List<VictoryFairyRank> findTopVictoryRanking(double m, double c, int year, final TeamFilter teamFilter, int limit);

    VictoryFairyRank findMyRanking(final double m, final double c, final Member targetMember, final int year,
                                   final TeamFilter teamFilter);

    int calculateMyRankingOrder(final double targetScore, final double m, final double c, final int year,
                                final TeamFilter teamFilter);
}
