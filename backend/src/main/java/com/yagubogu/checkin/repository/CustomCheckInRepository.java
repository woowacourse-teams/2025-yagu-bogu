package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.GameWithFanCountsResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountResponse;
import com.yagubogu.checkin.dto.StatCounts;
import com.yagubogu.checkin.dto.VictoryFairyCountResult;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.stat.dto.OpponentWinRateRow;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.List;

public interface CustomCheckInRepository {

    StatCounts findStatCounts(Member member, int year);

    int findWinCounts(Member member, int year);

    int findLoseCounts(Member member, int year);

    int findDrawCounts(Member member, int year);

    List<VictoryFairyCountResult> findCheckInAndWinCountBatch(List<Long> memberIds, int year);

    int countByMemberAndYear(Member member, int year);

    List<CheckInGameResponse> findCheckInHistory(
            Member member,
            Team team,
            int year,
            CheckInResultFilter resultFilter,
            CheckInOrderFilter orderFilter
    );

    List<GameWithFanCountsResponse> findGamesWithFanCountsByDate(LocalDate date);

    AverageStatistic findAverageStatistic(Member member);

    List<StadiumCheckInCountResponse> findStadiumCheckInCounts(
            Member member,
            int year
    );

    List<OpponentWinRateRow> findOpponentWinRates(
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
