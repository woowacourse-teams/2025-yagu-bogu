package com.yagubogu.stat.service;

import com.yagubogu.checkin.dto.StatCountsParam;
import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.checkin.dto.v1.VictoryFairyRankingResponse;
import com.yagubogu.checkin.dto.v1.VictoryFairyRankingResponse.VictoryFairyRankingParam;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stat.dto.AverageStatisticParam;
import com.yagubogu.stat.dto.CheckInSummaryParam;
import com.yagubogu.stat.dto.OpponentWinRateRowParam;
import com.yagubogu.stat.dto.OpponentWinRateTeamParam;
import com.yagubogu.stat.dto.StadiumStatsParam;
import com.yagubogu.stat.dto.VictoryFairySummaryParam;
import com.yagubogu.stat.dto.v1.AverageStatisticResponse;
import com.yagubogu.stat.dto.v1.LuckyStadiumResponse;
import com.yagubogu.stat.dto.v1.OpponentWinRateResponse;
import com.yagubogu.stat.dto.v1.RecentGamesWinRateResponse;
import com.yagubogu.stat.dto.v1.StatCountsResponse;
import com.yagubogu.stat.dto.v1.WinRateResponse;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StatService {

    private static final int RECENT_LIMIT = 10;
    private static final int VICTORY_RANKING_LIMIT = 5;
    private static final Comparator<OpponentWinRateTeamParam> OPPONENT_WIN_RATE_TEAM_COMPARATOR = Comparator
            .comparingDouble(
                    OpponentWinRateTeamParam::winRate)
            .reversed()
            .thenComparing(OpponentWinRateTeamParam::name);

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final VictoryFairyRankingRepository victoryFairyRankingRepository;

    public StatCountsResponse findStatCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        StatCountsParam statCountsParam = checkInRepository.findStatCounts(member, year);
        int favoriteCheckInCounts =
                statCountsParam.winCounts() + statCountsParam.drawCounts() + statCountsParam.loseCounts();

        return new StatCountsResponse(
                statCountsParam.winCounts(),
                statCountsParam.drawCounts(),
                statCountsParam.loseCounts(),
                favoriteCheckInCounts
        );
    }

    public WinRateResponse findWinRate(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        int winCounts = checkInRepository.findWinCounts(member, year);
        int loseCounts = checkInRepository.findLoseCounts(member, year);
        int favoriteCheckInCounts = winCounts + loseCounts;

        return new WinRateResponse(calculateWinRate(winCounts, favoriteCheckInCounts));
    }

    public RecentGamesWinRateResponse findRecentTenGamesWinRate(final Long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        int recentWinCounts = checkInRepository.findRecentGamesWinCounts(member, year, RECENT_LIMIT);
        int recentLoseCounts = checkInRepository.findRecentGamesLoseCounts(member, year, RECENT_LIMIT);
        int recentCounts = recentWinCounts + recentLoseCounts;

        return new RecentGamesWinRateResponse(calculateWinRate(recentWinCounts, recentCounts));
    }

    public LuckyStadiumResponse findLuckyStadium(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        List<StadiumStatsParam> stadiumStats = checkInRepository.findWinAndNonDrawCountByStadium(
                memberId,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
        String luckyStadiumName = getLuckyStadiumName(stadiumStats);

        return new LuckyStadiumResponse(luckyStadiumName);
    }

    public AverageStatisticResponse findAverageStatistic(final long memberId) {
        Member member = getMember(memberId);
        AverageStatisticParam averageStatisticParam = checkInRepository.findAverageStatistic(member);

        return AverageStatisticResponse.from(averageStatisticParam);
    }

    public OpponentWinRateResponse findOpponentWinRate(final Long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);
        Team team = member.getTeam();
        List<OpponentWinRateRowParam> winRates = checkInRepository.findOpponentWinRates(member, team, year);
        List<OpponentWinRateTeamParam> responses = getOpponentWinRateTeamResponse(winRates);

        return new OpponentWinRateResponse(responses);
    }

    private String getLuckyStadiumName(final List<StadiumStatsParam> stadiumStats) {
        double lowestWinRate = 0;
        String luckyStadiumName = null;
        for (StadiumStatsParam stadiumStatsParam : stadiumStats) {
            long winCounts = stadiumStatsParam.winCounts();
            long totalCountsWithoutDraw = stadiumStatsParam.totalCountsWithoutDraw();

            double currentWinRate = calculateWinRate(winCounts, totalCountsWithoutDraw);
            if (currentWinRate > lowestWinRate) {
                lowestWinRate = currentWinRate;
                luckyStadiumName = stadiumStatsParam.stadiumName();
            }
        }
        return luckyStadiumName;
    }

    @Transactional
    public void calculateVictoryFairyScore(final int year, final long gameId) {
        double m = checkInRepository.calculateTotalAverageWinRate(year);
        double c = checkInRepository.calculateAverageCheckInCount(year);

        // 오늘 인증한 member 조회
        List<Long> winMembers = checkInRepository.findWinMemberIdByGameId(gameId);
        if (!winMembers.isEmpty()) {
            victoryFairyRankingRepository.upsertDelta(m, c, winMembers, 1, 1, year);
        }
        List<Long> loseMembers = checkInRepository.findLoseMemberIdByGameId(gameId);
        if (!loseMembers.isEmpty()) {
            victoryFairyRankingRepository.upsertDelta(m, c, loseMembers, 0, 1, year);
        }
        List<Long> drawMembers = checkInRepository.findDrawMemberIdByGameId(gameId);
        if (!drawMembers.isEmpty()) {
            victoryFairyRankingRepository.upsertDelta(m, c, drawMembers, 0, 0, year);
        }
    }


    public VictoryFairyRankingResponse findVictoryFairyRankings(
            final long memberId,
            final TeamFilter teamFilter,
            Integer year
    ) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        Member member = getMember(memberId);
        List<VictoryFairyRankingParam> topRankingResponses = findTopVictoryRanking(teamFilter, year);

        VictoryFairyRankingParam myRankingResponse = victoryFairyRankingRepository.findByMemberAndTeamFilterAndYear(
                        member,
                        teamFilter,
                        year
                )
                .map(VictoryFairyRankingParam::from)
                .orElseGet(() -> VictoryFairyRankingParam.emptyRanking(member));

        return new VictoryFairyRankingResponse(topRankingResponses, myRankingResponse);
    }


    private List<VictoryFairyRankingParam> findTopVictoryRanking(
            final TeamFilter teamFilter,
            final int year
    ) {
        List<VictoryFairyRankParam> victoryFairyRankings = victoryFairyRankingRepository.findTopRankingByTeamFilterAndYear(
                teamFilter,
                VICTORY_RANKING_LIMIT,
                year
        );

        return VictoryFairyRankingParam.from(victoryFairyRankings);
    }

    public CheckInSummaryParam findCheckInSummary(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        StatCountsParam statCounts = checkInRepository.findStatCounts(member, year);
        LocalDate recentCheckInDate = checkInRepository.findRecentCheckInGameDate(member);
        int totalGamesForWinRate = statCounts.winCounts() + statCounts.loseCounts();
        double winRate = calculateWinRate(statCounts.winCounts(), totalGamesForWinRate);

        return CheckInSummaryParam.from(statCounts, winRate, recentCheckInDate);
    }

    public VictoryFairySummaryParam findVictoryFairySummary(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        return victoryFairyRankingRepository.findByMemberAndTeamFilterAndYear(member, TeamFilter.ALL, year)
                .map(overallRankInfo -> {
                    Integer teamRank = victoryFairyRankingRepository.findRankWithinTeamByMemberAndYear(member, year)
                            .orElse(null);
                    return VictoryFairySummaryParam.from(overallRankInfo, teamRank);
                })
                .orElseGet(VictoryFairySummaryParam::empty);
    }

    private double calculateWinRate(final long winCounts, final long favoriteCheckInCounts) {
        if (favoriteCheckInCounts == 0) {
            return 0;
        }
        double rate = (double) winCounts / favoriteCheckInCounts * 100;

        return Math.round(rate * 10) / 10.0;
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private void validateUser(final Member member) {
        if (member.isAdmin()) {
            throw new ForbiddenException("Member should not be admin");
        }
        if (member.getTeam() == null) {
            throw new UnprocessableEntityException("Team should not be null");
        }
    }

    private List<OpponentWinRateTeamParam> getOpponentWinRateTeamResponse(
            List<OpponentWinRateRowParam> winRates
    ) {
        return winRates.stream()
                .map(row -> {
                    long totalGames = row.wins() + row.losses();
                    double winRate = calculateWinRate(row.wins(), totalGames);

                    return new OpponentWinRateTeamParam(
                            row.teamId(),
                            row.name(),
                            row.shortName(),
                            row.teamCode(),
                            row.wins(),
                            row.losses(),
                            row.draws(),
                            winRate
                    );
                })
                .sorted(OPPONENT_WIN_RATE_TEAM_COMPARATOR)
                .toList();
    }
}
