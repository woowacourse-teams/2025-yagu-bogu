package com.yagubogu.stat.service;

import com.yagubogu.checkin.dto.StatCounts;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.pastcheckin.repository.PastCheckInRepository;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.OpponentWinRateResponse;
import com.yagubogu.stat.dto.OpponentWinRateRow;
import com.yagubogu.stat.dto.OpponentWinRateTeamResponse;
import com.yagubogu.stat.dto.RecentGamesWinRateResponse;
import com.yagubogu.stat.dto.StadiumStatsDto;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StatService {

    private static final int RECENT_LIMIT = 10;

    private static final Comparator<OpponentWinRateTeamResponse> OPPONENT_WIN_RATE_TEAM_COMPARATOR = Comparator.comparingDouble(
                    OpponentWinRateTeamResponse::winRate)
            .reversed()
            .thenComparing(OpponentWinRateTeamResponse::name);

    private final CheckInRepository checkInRepository;
    private final PastCheckInRepository pastCheckInRepository;
    private final MemberRepository memberRepository;

    public StatCountsResponse findStatCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        StatCounts statCounts = checkInRepository.findStatCounts(member, year);
        int favoriteCheckInCounts = statCounts.winCounts() + statCounts.drawCounts() + statCounts.loseCounts();

        return new StatCountsResponse(
                statCounts.winCounts(),
                statCounts.drawCounts(),
                statCounts.loseCounts(),
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

        List<StadiumStatsDto> combinedStats = getCombinedStadiumStats(memberId, year);

        double lowestWinRate = 0;
        String luckyStadiumName = null;
        for (StadiumStatsDto stadiumStatsDto : combinedStats) {
            long winCounts = stadiumStatsDto.winCounts();
            long totalCountsWithoutDraw = stadiumStatsDto.totalCountsWithoutDraw();

            double currentWinRate = calculateWinRate(winCounts, totalCountsWithoutDraw);
            if (currentWinRate > lowestWinRate) {
                lowestWinRate = currentWinRate;
                luckyStadiumName = stadiumStatsDto.stadiumName();
            }
        }

        return new LuckyStadiumResponse(luckyStadiumName);
    }

    private List<StadiumStatsDto> getCombinedStadiumStats(final long memberId, final int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<StadiumStatsDto> checkInStats = checkInRepository.findWinAndNonDrawCountByStadium(
                memberId, startDate, endDate
        );
        List<StadiumStatsDto> pastCheckInStats = pastCheckInRepository.findWinAndNonDrawCountByStadiumFromPastCheckIn(
                memberId, startDate, endDate
        );

        Map<String, StadiumStatsDto> statsMap = new HashMap<>();
        for (StadiumStatsDto stat : checkInStats) {
            statsMap.put(stat.stadiumName(), stat);
        }

        for (StadiumStatsDto stat : pastCheckInStats) {
            if (statsMap.containsKey(stat.stadiumName())) {
                StadiumStatsDto existing = statsMap.get(stat.stadiumName());
                StadiumStatsDto combined = new StadiumStatsDto(
                        stat.stadiumName(),
                        existing.winCounts() + stat.winCounts(),
                        existing.totalCountsWithoutDraw() + stat.totalCountsWithoutDraw()
                );
                statsMap.put(stat.stadiumName(), combined);
            } else {
                statsMap.put(stat.stadiumName(), stat);
            }
        }

        return new ArrayList<>(statsMap.values());
    }

    public AverageStatisticResponse findAverageStatistic(final long memberId) {
        Member member = getMember(memberId);
        AverageStatistic averageStatistic = checkInRepository.findAverageStatistic(member);

        return AverageStatisticResponse.from(averageStatistic);
    }

    public OpponentWinRateResponse findOpponentWinRate(final Long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);
        Team team = member.getTeam();
        List<OpponentWinRateRow> winRates = checkInRepository.findOpponentWinRates(member, team, year);
        List<OpponentWinRateTeamResponse> responses = getOpponentWinRateTeamResponse(winRates);

        return new OpponentWinRateResponse(responses);
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

    private List<OpponentWinRateTeamResponse> getOpponentWinRateTeamResponse(
            List<OpponentWinRateRow> winRates
    ) {
        return winRates.stream()
                .map(row -> {
                    long totalGames = row.wins() + row.losses();
                    double winRate = calculateWinRate(row.wins(), totalGames);

                    return new OpponentWinRateTeamResponse(
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
