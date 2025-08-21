package com.yagubogu.stat.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.AverageStatistic;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.OpponentWinRateResponse;
import com.yagubogu.stat.dto.OpponentWinRateRow;
import com.yagubogu.stat.dto.OpponentWinRateTeamResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
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

    private static final Comparator<OpponentWinRateTeamResponse> OPPONENT_WIN_RATE_TEAM_COMPARATOR = Comparator.comparingDouble(
                    OpponentWinRateTeamResponse::winRate)
            .reversed()
            .thenComparing(OpponentWinRateTeamResponse::name);

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final TeamRepository teamRepository;

    public StatCountsResponse findStatCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        int winCounts = checkInRepository.findWinCounts(member, year);
        int drawCounts = checkInRepository.findDrawCounts(member, year);
        int loseCounts = checkInRepository.findLoseCounts(member, year);
        int favoriteCheckInCounts = winCounts + drawCounts + loseCounts;

        return new StatCountsResponse(winCounts, drawCounts, loseCounts, favoriteCheckInCounts);
    }

    public WinRateResponse findWinRate(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        int winCounts = checkInRepository.findWinCounts(member, year);
        int drawCounts = checkInRepository.findDrawCounts(member, year);
        int loseCounts = checkInRepository.findLoseCounts(member, year);
        int favoriteCheckInCounts = winCounts + drawCounts + loseCounts;

        return new WinRateResponse(calculateWinRate(winCounts, favoriteCheckInCounts));
    }

    public LuckyStadiumResponse findLuckyStadium(final long memberId, final int year) {
        Member member = getMember(memberId);
        validateUser(member);

        List<Stadium> stadiums = stadiumRepository.findAll();
        double lowestWinRate = 0;
        Stadium luckyStadium = null;
        for (Stadium stadium : stadiums) {
            int winCounts = checkInRepository.countWinsFavoriteTeamByStadiumAndMember(stadium, member, year);
            int totalCounts = checkInRepository.countTotalFavoriteTeamGamesByStadiumAndMember(stadium, member,
                    year);

            double currentWinRate = calculateWinRate(winCounts, totalCounts);
            if (currentWinRate > lowestWinRate) {
                lowestWinRate = currentWinRate;
                luckyStadium = stadium;
            }
        }

        return LuckyStadiumResponse.from(luckyStadium);
    }

    public AverageStatisticResponse findAverageStatistic(final long memberId) {
        Member member = getMember(memberId);
        AverageStatistic averageStatistic = checkInRepository.findAverageStatistic(member);

        return AverageStatisticResponse.from(averageStatistic);
    }

    public OpponentWinRateResponse findOpponentWinRate(final Long memberId, final int year) {
        Long myTeamId = getTeamIdByMemberId(memberId);
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<OpponentWinRateRow> home = checkInRepository.findOpponentWinRatesWhenHome(myTeamId, start, end);
        List<OpponentWinRateRow> away = checkInRepository.findOpponentWinRatesWhenAway(myTeamId, start, end);
        Map<Long, OpponentWinRateRow> mergedWinRate = mergeByTeamId(home, away);

        List<Team> opponents = teamRepository.findOpponentsExcluding(myTeamId);
        List<OpponentWinRateTeamResponse> responses = getOpponentWinRateTeamResponse(mergedWinRate, opponents);

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
    }

    private Long getTeamIdByMemberId(final Long memberId) {
        return memberRepository.findTeamIdById(memberId)
                .orElseThrow(() -> new NotFoundException("Team not exist"));
    }

    private Map<Long, OpponentWinRateRow> mergeByTeamId(
            final List<OpponentWinRateRow> home,
            final List<OpponentWinRateRow> away
    ) {
        Map<Long, OpponentWinRateRow> mergedWinRate = new HashMap<>();
        for (OpponentWinRateRow r : home) {
            mergedWinRate.put(r.teamId(), r);
        }
        for (OpponentWinRateRow r : away) {
            mergedWinRate.merge(r.teamId(), r, (a, b) ->
                    new OpponentWinRateRow(
                            a.teamId(), a.name(), a.shortName(), a.teamCode(), a.wins() + b.wins(),
                            a.games() + b.games()
                    )
            );
        }
        return mergedWinRate;
    }

    private List<OpponentWinRateTeamResponse> getOpponentWinRateTeamResponse(
            final Map<Long, OpponentWinRateRow> merged,
            final List<Team> opponents
    ) {
        return opponents.stream()
                .map(op -> {
                    OpponentWinRateRow row = merged.get(op.getId());
                    if (row == null) {
                        return new OpponentWinRateTeamResponse(
                                op.getId(), op.getName(), op.getShortName(), op.getTeamCode(), 0.0
                        );
                    }
                    double winRate = calculateWinRate(row.wins(), row.games());

                    return new OpponentWinRateTeamResponse(
                            row.teamId(), row.name(), row.shortName(), row.teamCode(), winRate
                    );
                })
                .sorted(OPPONENT_WIN_RATE_TEAM_COMPARATOR)
                .toList();
    }
}
