package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanCountsByGameResponse;
import com.yagubogu.checkin.dto.FanRateByGameResponse;
import com.yagubogu.checkin.dto.FanRateGameEntry;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingEntryResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckInService {

    private static final int TOP_RANKINGS = 5;
    private static final int NOT_FOUND = -1;
    private static final int FOUND = 1;

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;

    public void createCheckIn(final Long memberId, final CreateCheckInRequest request) {
        long stadiumId = request.stadiumId();
        Stadium stadium = getStadiumById(stadiumId);
        LocalDate date = request.date();
        Game game = getGame(stadium, date);

        Member member = getMember(memberId);
        Team team = member.getTeam();

        checkInRepository.save(new CheckIn(game, member, team));
    }

    public FanRateResponse findFanRatesByGames(final long memberId, final LocalDate date) {
        Member member = getMember(memberId);
        Team myTeam = member.getTeam();
        List<Game> games = gameRepository.findGameByDate(date);

        List<FanRateGameEntry> fanRateGameEntries = new ArrayList<>();
        FanRateByGameResponse myTeamGame = null;

        for (Game game : games) {
            FanCountsByGameResponse counts = getFanCountsForGame(game);
            FanRateByGameResponse fanRateByGameResponse = createFanRateByGameResponse(game, counts);

            if (game.hasTeam(myTeam)) {
                myTeamGame = fanRateByGameResponse;
                continue;
            }
            fanRateGameEntries.add(new FanRateGameEntry(counts.totalCheckInCounts(), fanRateByGameResponse));
        }

        return FanRateResponse.from(myTeamGame, fanRateGameEntries);
    }

    public CheckInCountsResponse findCheckInCounts(final long memberId, final long year) {
        Member member = getMember(memberId);
        int checkInCounts = checkInRepository.countByMemberAndYear(member, year);

        return new CheckInCountsResponse(checkInCounts);
    }

    public CheckInHistoryResponse findCheckInHistory(
            final long memberId,
            final int year,
            final CheckInResultFilter filter
    ) {
        Member member = getMember(memberId);
        Team team = member.getTeam();

        return switch (filter) {
            case ALL -> new CheckInHistoryResponse(checkInRepository.findCheckInHistory(member, team, year));
            case WIN -> new CheckInHistoryResponse(checkInRepository.findCheckInWinHistory(member, team, year));
        };
    }

    public VictoryFairyRankingResponses findVictoryFairyRankings(final long memberId) {
        List<VictoryFairyRankingEntryResponse> sortedList = getSortedRankingList();

        int myRanking = findMyRankingIndex(sortedList, memberId);
        VictoryFairyRankingEntryResponse myRankingData = findMyRanking(sortedList, memberId);

        List<VictoryFairyRankingEntryResponse> topRankings = sortedList.stream()
                .limit(TOP_RANKINGS)
                .toList();

        return VictoryFairyRankingResponses.from(topRankings, myRankingData, myRanking);
    }

    private List<VictoryFairyRankingEntryResponse> getSortedRankingList() {
        List<VictoryFairyRankingEntryResponse> memberCheckIns = checkInRepository.findVictoryFairyRankingCandidates();

        return memberCheckIns.stream()
                .sorted(Comparator
                        // 1. 승률 먼저 정렬
                        .comparingDouble(VictoryFairyRankingEntryResponse::winPercent).reversed()
                        // 2. 직관 횟수 정렬
                        .thenComparing(Comparator.comparing(VictoryFairyRankingEntryResponse::totalCheckIns).reversed())
                        // 3. 닉네임순 정렬
                        .thenComparing(VictoryFairyRankingEntryResponse::nickname)
                )
                .toList();
    }

    private int findMyRankingIndex(
            final List<VictoryFairyRankingEntryResponse> sortedList,
            final long memberId
    ) {
        return IntStream.range(0, sortedList.size())
                .filter(i -> sortedList.get(i).memberId().equals(memberId))
                .findFirst()
                .orElse(NOT_FOUND) + FOUND;
    }

    private VictoryFairyRankingEntryResponse findMyRanking(
            final List<VictoryFairyRankingEntryResponse> sortedList,
            final long memberId
    ) {
        return sortedList.stream()
                .filter(d -> d.memberId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    public CheckInStatusResponse findCheckInStatus(final long memberId, final LocalDate date) {
        Member member = getMember(memberId);
        boolean isCheckIn = checkInRepository.existsByMemberAndGameDate(member, date);

        return new CheckInStatusResponse(isCheckIn);
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

    private FanCountsByGameResponse getFanCountsForGame(final Game game) {
        return checkInRepository.countTotalAndHomeTeamAndAwayTeam(
                game,
                game.getHomeTeam(),
                game.getAwayTeam()
        );
    }

    private FanRateByGameResponse createFanRateByGameResponse(
            final Game game,
            final FanCountsByGameResponse counts
    ) {
        long total = counts.totalCheckInCounts();
        double homeRate = calculateRoundRate(counts.homeTeamCheckInCounts(), total);
        double awayRate = calculateRoundRate(counts.awayTeamCheckInCounts(), total);

        return FanRateByGameResponse.from(game, total, homeRate, awayRate);
    }

    private double calculateRoundRate(final long checkInCounts, final long total) {
        if (total == 0) {
            return 0.0;
        }

        return Math.round(((double) checkInCounts / total) * 1000) / 10.0;
    }
}
