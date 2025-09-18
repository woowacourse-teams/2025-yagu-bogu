package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import java.util.List;

public interface CustomCheckInRepository {

    double calculateTotalAverageWinRate(int year);

    double calculateAverageCheckInCount(int year);

    List<VictoryFairyRank> findTopVictoryRanking(double m, double c, int year, TeamFilter teamFilter, int limit);

    VictoryFairyRank findMyRanking(double m, double c, Member targetMember, final int year, TeamFilter teamFilter);

    int calculateMyRankingOrder(double targetScore, double m, double c, int year, TeamFilter teamFilter);
}
