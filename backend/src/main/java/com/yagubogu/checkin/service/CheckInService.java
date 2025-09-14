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
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CheckInService {

    private static final int TOP_RANKINGS = 5;
    private static final int NOT_FOUND = -1;
    private static final int FOUND = 1;

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;

    @Transactional
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

    public CheckInCountsResponse findCheckInCounts(final long memberId, final long year) {
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

        List<CheckInGameResponse> checkIns = switch (resultFilter) {
            case ALL -> checkInRepository.findCheckInHistory(member, team, year);
            case WIN -> checkInRepository.findCheckInWinHistory(member, team, year);
        };

        if (orderFilter.isOldest()) {
            Collections.reverse(checkIns);
        }

        return new CheckInHistoryResponse(checkIns);
    }

    public VictoryFairyRankingResponses findVictoryFairyRankings(final long memberId, final TeamFilter teamCode) {
        Member member = getMember(memberId);

        // m : 전체 유저 평균 승롤 (전체 완료된 경기의 인증 중 승수 / 전체 완료된 경기의 인증수)
        double m = checkInRepository.calculateTotalAverageWinRate(2025);
        // c
        double c = checkInRepository.calculateAverageCheckInCount(2025);

        System.out.println(m + " " + c);
        if (teamCode == TeamFilter.ALL) {
           // 모든 팀 팬들을 고려한 승요 랭킹
        }
        // 응원팀 승요 랭킹
        teamRepository.findByTeamCode(teamCode.name());

               // 승요 점수 별 멤버 정렬해서 반환


        List<VictoryFairyRankingEntryResponse> sortedList = getSortedRankingList();
        int myRanking = findMyRankingIndex(sortedList, memberId);
        VictoryFairyRankingEntryResponse myRankingData = findMyRanking(sortedList, memberId);

        List<VictoryFairyRankingEntryResponse> topRankings = sortedList.stream()
                .limit(TOP_RANKINGS)
                .toList();

        return VictoryFairyRankingResponses.from(topRankings, myRankingData, myRanking);
    }

    public StadiumCheckInCountsResponse findStadiumCheckInCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<StadiumCheckInCountResponse> stadiumCheckInCounts = checkInRepository.findStadiumCheckInCounts(member,
                start, end);

        return new StadiumCheckInCountsResponse(stadiumCheckInCounts);
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
            final List<VictoryFairyRankingEntryResponse> sortedResponses,
            final long memberId
    ) {
        return IntStream.range(0, sortedResponses.size())
                .filter(i -> sortedResponses.get(i).memberId().equals(memberId))
                .findFirst()
                .orElse(NOT_FOUND) + FOUND;
    }

    private VictoryFairyRankingEntryResponse findMyRanking(
            final List<VictoryFairyRankingEntryResponse> sortedResponses,
            final long memberId
    ) {
        return sortedResponses.stream()
                .filter(d -> d.memberId().equals(memberId))
                .findFirst()
                .orElse(VictoryFairyRankingEntryResponse.generateEmptyRankingFor(getMember(memberId)));
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

    private FanRateByGameResponse createFanRateByGameResponse(final GameWithFanCountsResponse gameWithFanCounts) {
        Long total = gameWithFanCounts.totalCheckInCounts();
        double homeRate = calculateRoundRate(gameWithFanCounts.homeTeamCheckInCounts(), total);
        double awayRate = calculateRoundRate(gameWithFanCounts.awayTeamCheckInCounts(), total);

        return FanRateByGameResponse.from(gameWithFanCounts.game(), total, homeRate, awayRate);
    }

    private double calculateRoundRate(final Long checkInCounts, final Long total) {
        if (total == 0 || checkInCounts == 0) {
            return 0.0;
        }

        return Math.round(((double) checkInCounts / total) * 1000) / 10.0;
    }
}
