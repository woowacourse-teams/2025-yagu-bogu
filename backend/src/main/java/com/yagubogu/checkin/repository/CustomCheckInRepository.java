package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.member.domain.Member;
import java.util.List;

public interface CustomCheckInRepository {

    int findWinCounts(Member member, int year);

    int findLoseCounts(Member member, int year);

    int findDrawCounts(Member member, int year);

    int findRecentGamesWinCounts(Member member, int year, int limit);

    int findRecentGamesLoseCounts(Member member, int year, int limit);

    int findRecentGamesDrawCounts(Member member, int year, int limit);

    VictoryFairyRank findMyRanking(double m, double c, Member targetMember, int year, TeamFilter teamFilter);

    List<VictoryFairyRank> findTopVictoryRanking(double m, double c, int year, TeamFilter teamFilter, int limit);

    int calculateMyRankingOrder(double targetScore, double m, double c, int year, TeamFilter teamFilter);

    double calculateAverageCheckInCount(int year);

    double calculateTotalAverageWinRate(int year);
}
