package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateByGameResponse;
import com.yagubogu.checkin.dto.FanRateGameEntry;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.GameWithFanCountsResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountsResponse;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses.VictoryFairyRankingResponse;
import com.yagubogu.checkin.event.CheckInEvent;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.sse.dto.CheckInCreatedEvent;
import com.yagubogu.sse.dto.GameWithFanRateResponse;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CheckInService {

    private static final int VICTORY_RANKING_LIMIT = 5;
    private static final double ROUND_FACTOR = 10.0;

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createCheckIn(final Long memberId, final CreateCheckInRequest request) {
        long stadiumId = request.stadiumId();
        Stadium stadium = getStadiumById(stadiumId);
        LocalDate date = request.date();
        Game game = getGame(stadium, date);

        Member member = getMember(memberId);
        Team team = member.getTeam();

        CheckIn checkIn = new CheckIn(game, member, team);
        checkInRepository.save(checkIn);

        applicationEventPublisher.publishEvent(new CheckInCreatedEvent(date));
        checkInRepository.save(new CheckIn(game, member, team));
        applicationEventPublisher.publishEvent(new CheckInEvent(member));
    }

    public FanRateResponse findFanRatesByGames(final long memberId, final LocalDate date) {
        Member member = getMember(memberId);
        Team myTeam = member.getTeam();

        List<FanRateGameEntry> fanRatesByGames = new ArrayList<>();
        FanRateByGameResponse myFanRateByGame = null;
        List<GameWithFanCountsResponse> gameWithFanCounts = checkInRepository.findGamesWithFanCountsByDate(date);

        for (GameWithFanCountsResponse gameWithFanCount : gameWithFanCounts) {
            Game game = gameWithFanCount.game();
            FanRateByGameResponse response = createFanRateByGameResponse(gameWithFanCount);

            if (game.hasTeam(myTeam)) {
                myFanRateByGame = createFanRateByGameResponse(gameWithFanCount);
                continue;
            }
            fanRatesByGames.add(new FanRateGameEntry(gameWithFanCount.totalCheckInCounts(), response));
        }

        return FanRateResponse.from(myFanRateByGame, fanRatesByGames);
    }

    public CheckInCountsResponse findCheckInCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        int checkInCounts = checkInRepository.countByMemberAndYear(member, year);

        return new CheckInCountsResponse(checkInCounts);
    }

    public CheckInHistoryResponse findCheckInHistory(
            final long memberId,
            final int year,
            final CheckInResultFilter resultFilter,
            final CheckInOrderFilter orderFilter
    ) {
        Member member = getMember(memberId);
        Team team = member.getTeam();
        List<CheckInGameResponse> checkIns = checkInRepository.findCheckInHistory(
                member,
                team,
                year,
                resultFilter,
                orderFilter
        );

        return new CheckInHistoryResponse(checkIns);
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
        double m = checkInRepository.calculateTotalAverageWinRate(year);
        double c = checkInRepository.calculateAverageCheckInCount(year);

        List<VictoryFairyRankingResponse> topRankingResponses = findTopVictoryRanking(teamFilter, year, m, c);
        VictoryFairyRankingResponse myRankingResponse = findMyVictoryRanking(teamFilter, year, m, c, member);

        return new VictoryFairyRankingResponses(topRankingResponses, myRankingResponse);
    }

    public StadiumCheckInCountsResponse findStadiumCheckInCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        List<StadiumCheckInCountResponse> stadiumCheckInCounts = checkInRepository.findStadiumCheckInCounts(member,
                year);

        return new StadiumCheckInCountsResponse(stadiumCheckInCounts);
    }

    public CheckInStatusResponse findCheckInStatus(final long memberId, final LocalDate date) {
        Member member = getMember(memberId);
        boolean isCheckIn = checkInRepository.existsByMemberAndGameDate(member, date);

        return new CheckInStatusResponse(isCheckIn);
    }

    public List<GameWithFanRateResponse> buildCheckInEventData(final LocalDate date) {
        List<GameWithFanRateResponse> result = new ArrayList<>();

        List<GameWithFanCountsResponse> responses = checkInRepository.findGamesWithFanCountsByDate(date);
        for (GameWithFanCountsResponse response : responses) {
            Game game = response.game();
            long homeTeamCounts = response.homeTeamCheckInCounts();
            long awayTeamCounts = response.awayTeamCheckInCounts();
            long totalCounts = response.totalCheckInCounts();

            double homeTeamRate = calculateRoundRate(homeTeamCounts, totalCounts);
            double awayTeamRate = calculateRoundRate(awayTeamCounts, totalCounts);
            result.add(GameWithFanRateResponse.from(game, homeTeamRate, awayTeamRate));
        }

        return result;
    }

    private List<VictoryFairyRankingResponse> findTopVictoryRanking(final TeamFilter teamFilter,
                                                                    final int year, final double m,
                                                                    final double c) {
        List<VictoryFairyRank> topRanking = checkInRepository.findTopVictoryRanking(m, c, year, teamFilter,
                VICTORY_RANKING_LIMIT);
        double previousScore = -1.0;
        int ranking = 0;
        int count = 1;
        List<VictoryFairyRankingResponse> topRankingResponses = new ArrayList<>();
        for (VictoryFairyRank rank : topRanking) {
            double currentScore = rank.score();
            if (previousScore != currentScore) {
                ranking += count;
                count = 1;
            } else {
                count++;
            }
            double winPercent = Math.round(rank.winPercent() * ROUND_FACTOR) / ROUND_FACTOR;
            topRankingResponses.add(new VictoryFairyRankingResponse(
                    ranking, rank.nickname(),
                    rank.profileImageUrl(),
                    rank.teamShortName(),
                    winPercent,
                    Math.round(rank.score() * 100 * ROUND_FACTOR) / ROUND_FACTOR)
            );
            previousScore = currentScore;
        }
        return topRankingResponses;
    }

    private VictoryFairyRankingResponse findMyVictoryRanking(final TeamFilter teamFilter, final int year,
                                                             final double m, final double c,
                                                             final Member member) {
        VictoryFairyRank myRanking = checkInRepository.findMyRanking(m, c, member, year, teamFilter);
        double score = Math.round(myRanking.score() * 100 * ROUND_FACTOR) / ROUND_FACTOR;
        int myRankingOrder = checkInRepository.calculateMyRankingOrder(myRanking.score(), m, c, year, teamFilter) + 1;
        double winPercent = Math.round(myRanking.winPercent() * ROUND_FACTOR) / ROUND_FACTOR;

        return new VictoryFairyRankingResponse(
                myRankingOrder,
                myRanking.nickname(),
                myRanking.profileImageUrl(),
                myRanking.teamShortName(),
                winPercent,
                score
        );
    }

    private Stadium getStadiumById(final long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium is not found"));
    }

    private Game getGame(final Stadium stadium, final LocalDate date) {
        return gameRepository.findByStadiumAndDate(stadium, date)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private FanRateByGameResponse createFanRateByGameResponse(final GameWithFanCountsResponse gameWithFanCounts) {
        Long total = gameWithFanCounts.totalCheckInCounts();
        double homeRate = calculateRoundRate(gameWithFanCounts.homeTeamCheckInCounts(), total);
        double awayRate = calculateRoundRate(gameWithFanCounts.awayTeamCheckInCounts(), total);

        return FanRateByGameResponse.from(gameWithFanCounts.game(), total, homeRate, awayRate);
    }

    private double calculateRoundRate(final Long checkInCounts, final Long total) {
        if (total == 0) {
            return 0.0;
        }

        return Math.round(((double) checkInCounts / total) * 1000) / ROUND_FACTOR;
    }
}
