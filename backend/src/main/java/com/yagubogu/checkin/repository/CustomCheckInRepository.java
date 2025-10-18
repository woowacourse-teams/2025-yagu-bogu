package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInGameParam;
import com.yagubogu.checkin.dto.GameWithFanCountsParam;
import com.yagubogu.checkin.dto.StadiumCheckInCountParam;
import com.yagubogu.checkin.dto.StatCountsParam;
import com.yagubogu.checkin.dto.VictoryFairyCountResult;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.AverageStatisticParam;
import com.yagubogu.stat.dto.OpponentWinRateRowParam;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.List;

public interface CustomCheckInRepository {

    LocalDate findRecentCheckInGameDate(Member member);

    StatCountsParam findStatCounts(Member member, int year);

    int findWinCounts(Member member, int year);

    int findLoseCounts(Member member, int year);

    int findDrawCounts(Member member, int year);

    List<VictoryFairyCountResult> findCheckInAndWinCountBatch(List<Long> memberIds, int year);

    int countByMemberAndYear(Member member, int year);

    List<CheckInGameParam> findCheckInHistory(
            Member member,
            Team team,
            int year,
            CheckInResultFilter resultFilter,
            CheckInOrderFilter orderFilter
    );

    List<GameWithFanCountsParam> findGamesWithFanCountsByDate(LocalDate date);

    AverageStatisticParam findAverageStatistic(Member member);

    List<StadiumCheckInCountParam> findStadiumCheckInCounts(
            Member member,
            int year
    );

    List<OpponentWinRateRowParam> findOpponentWinRates(
            Member member,
            Team team,
            int year
    );

    double calculateTotalAverageWinRate(int year);

    double calculateAverageCheckInCount(int year);

    int findRecentGamesDrawCounts(Member member, int year, int limit);

    int findRecentGamesLoseCounts(Member member, int year, int limit);

    int findRecentGamesWinCounts(Member member, int year, int limit);

    List<Long> findWinMemberIdByGameId(long gameId);

    List<Long> findLoseMemberIdByGameId(long gameId);

    List<Long> findDrawMemberIdByGameId(long gameId);
}
