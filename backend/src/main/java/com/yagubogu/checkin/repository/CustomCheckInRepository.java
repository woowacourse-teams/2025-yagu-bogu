package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import java.util.List;

public interface CustomCheckInRepository {

    int findWinCounts(Member member, final int year);

    int findLoseCounts(Member member, final int year);

    int findDrawCounts(Member member, final int year);

    int findRecentGamesWinCounts(Member member, final int year, final int limit);

    int findRecentGamesLoseCounts(Member member, final int year, final int limit);

    int findRecentGamesDrawCounts(Member member, final int year, final int limit);

    VictoryFairyRank findMyRanking(double m, double c, Member targetMember, final int year, TeamFilter teamFilter);

    List<VictoryFairyRank> findTopVictoryRanking(double m, double c, int year, TeamFilter teamFilter, int limit);

    int calculateMyRankingOrder(double targetScore, double m, double c, int year, TeamFilter teamFilter);

    double calculateAverageCheckInCount(int year);

    double calculateTotalAverageWinRate(int year);
}
