package com.yagubogu.stat.service;

import com.yagubogu.checkin.dto.StatCounts;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses.VictoryFairyRankingResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.OpponentWinRateResponse;
import com.yagubogu.stat.dto.OpponentWinRateRow;
import com.yagubogu.stat.dto.OpponentWinRateTeamResponse;
import com.yagubogu.stat.dto.RecentGamesWinRateResponse;
import com.yagubogu.stat.dto.StadiumStatsDto;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.VictoryFairyChunkResult;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StatService {

    private static final int RECENT_LIMIT = 10;
    private static final int VICTORY_RANKING_LIMIT = 5;
    private static final int CHUNK_SIZE = 2000;
    private static final Comparator<OpponentWinRateTeamResponse> OPPONENT_WIN_RATE_TEAM_COMPARATOR = Comparator
            .comparingDouble(OpponentWinRateTeamResponse::winRate)
            .reversed()
            .thenComparing(OpponentWinRateTeamResponse::name);

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final VictoryFairyRankingRepository victoryFairyRankingRepository;
    private final VictoryFairyRankingSyncService victoryFairyRankingSyncService;

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

        List<StadiumStatsDto> hello = checkInRepository.findWinAndNonDrawCountByStadium(
                memberId,
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
        double lowestWinRate = 0;
        String luckyStadiumName = null;
        for (StadiumStatsDto stadiumStatsDto : hello) {
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

    @Transactional
    public void calculateVictoryScore(final int year, final long gameId) {
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

    public VictoryFairyRankingResponses findVictoryFairyRankings(
            final long memberId,
            final TeamFilter teamFilter,
            Integer year
    ) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        Member member = getMember(memberId);
        List<VictoryFairyRankingResponse> topRankingResponses = findTopVictoryRanking(teamFilter, year);

        VictoryFairyRankingResponse myRankingResponse = victoryFairyRankingRepository.findByMemberAndTeamFilterAndYear(
                        member,
                        teamFilter,
                        year
                )
                .map(VictoryFairyRankingResponse::from)
                .orElseGet(() -> VictoryFairyRankingResponse.emptyRanking(member));

        return new VictoryFairyRankingResponses(topRankingResponses, myRankingResponse);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void updateRankings(final LocalDate date) {
        int currentYear = date.getYear();
        int page = 0;
        int totalProcessed = 0;
        int totalUpdated = 0;
        int totalInserted = 0;

        try {
            Slice<Long> slice;
            do {
                Pageable pageable = PageRequest.of(page, CHUNK_SIZE);
                slice = checkInRepository.findDistinctMemberIdsByDate(date, pageable);

                if (slice.hasContent()) {
                    List<Long> memberIds = slice.getContent();

                    VictoryFairyChunkResult result = victoryFairyRankingSyncService.processChunk(memberIds,
                            currentYear);

                    totalProcessed += memberIds.size();
                    totalUpdated += result.updatedCount();
                    totalInserted += result.insertedCount();

                    log.info("Progress: page {}, {} members processed (updated: {}, inserted: {})",
                            page, totalProcessed, totalUpdated, totalInserted);
                }
                page++;

            } while (slice.hasNext());

            log.info("=== Batch Completed === total: {}, updated: {}, inserted: {}, skipped: {}",
                    totalProcessed, totalUpdated, totalInserted,
                    totalProcessed - totalUpdated - totalInserted);
        } catch (RuntimeException e) {
            log.error("Batch failed", e);
            throw e;
        }
    }

    private List<VictoryFairyRankingResponse> findTopVictoryRanking(
            final TeamFilter teamFilter,
            final int year
    ) {
        List<VictoryFairyRank> victoryFairyRankings = victoryFairyRankingRepository.findTopRankingByTeamFilterAndYear(
                teamFilter,
                VICTORY_RANKING_LIMIT,
                year
        );

        return VictoryFairyRankingResponse.from(victoryFairyRankings);
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
