package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.VictoryFairyRankingDataResponse;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CheckInService {

    private static final int TOP_FIVE = 5;

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;

    public void createCheckIn(final CreateCheckInRequest request) {
        long stadiumId = request.stadiumId();
        Stadium stadium = getStadiumById(stadiumId);
        LocalDate date = request.date();
        Game game = getGame(stadium, date);

        long memberId = request.memberId();
        Member member = getMember(memberId);
        Team team = member.getTeam();

        checkInRepository.save(new CheckIn(game, member, team));
    }

    public CheckInCountsResponse findCheckInCounts(final long memberId, final long year) {
        Member member = getMember(memberId);
        int checkInCounts = checkInRepository.countByMemberAndYear(member, year);

        return new CheckInCountsResponse(checkInCounts);
    }

    public CheckInHistoryResponse findCheckInHistory(final long memberId, final int year) {
        Member member = getMember(memberId);
        Team team = member.getTeam();

        List<CheckInGameResponse> checkInGameResponses = checkInRepository.findCheckInHistory(member, team, year);

        return new CheckInHistoryResponse(checkInGameResponses);
    }

    public VictoryFairyRankingResponses findVictoryFairyRankings(final long memberId) {
        List<VictoryFairyRankingDataResponse> sortedList = getSortedRankingList();

        int myRanking = findMyRanking(sortedList, memberId);
        VictoryFairyRankingDataResponse myRankingData = findMyRankingData(sortedList, memberId);

        List<VictoryFairyRankingDataResponse> top5 = sortedList.stream()
                .limit(TOP_FIVE)
                .toList();

        return VictoryFairyRankingResponses.from(top5, myRankingData, myRanking);
    }

    private List<VictoryFairyRankingDataResponse> getSortedRankingList() {
        List<VictoryFairyRankingDataResponse> memberCheckIns = checkInRepository.findGroupedMemberCheckinsBySameTeam();

        return memberCheckIns.stream()
                .sorted(Comparator
                        // 1. 승률 먼저 정렬
                        .comparingDouble(VictoryFairyRankingDataResponse::winPercent).reversed()
                        // 2. 직관 횟수 정렬
                        .thenComparing(Comparator.comparing(VictoryFairyRankingDataResponse::totalCheckIns).reversed())
                        // 3. 닉네임순 정렬
                        .thenComparing(VictoryFairyRankingDataResponse::nickname)
                )
                .toList();
    }

    private int findMyRanking(
            final List<VictoryFairyRankingDataResponse> sortedList,
            final long memberId
    ) {
        return IntStream.range(0, sortedList.size())
                .filter(i -> sortedList.get(i).memberId().equals(memberId))
                .findFirst()
                .orElse(-1) + 1;
    }

    private VictoryFairyRankingDataResponse findMyRankingData(
            final List<VictoryFairyRankingDataResponse> sortedList,
            final long memberId
    ) {
        return sortedList.stream()
                .filter(d -> d.memberId().equals(memberId))
                .findFirst()
                .orElse(null);
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
}
