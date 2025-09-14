package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;

public interface CustomCheckInRepository {

    double calculateTotalAverageWinRate(int year);

    double calculateAverageCheckInCount(int year);

    VictoryFairyRankingResponses findTopRankingAndMyRanking(double m, double c, int year);
}
