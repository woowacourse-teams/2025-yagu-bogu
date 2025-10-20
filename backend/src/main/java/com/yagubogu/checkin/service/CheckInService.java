package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.checkin.dto.CheckInGameParam;
import com.yagubogu.checkin.dto.FanRateByGameParam;
import com.yagubogu.checkin.dto.FanRateGameParam;
import com.yagubogu.checkin.dto.GameWithFanCountsParam;
import com.yagubogu.checkin.dto.StadiumCheckInCountParam;
import com.yagubogu.checkin.dto.event.CheckInEvent;
import com.yagubogu.checkin.dto.event.StadiumVisitEvent;
import com.yagubogu.checkin.dto.v1.CheckInCountsResponse;
import com.yagubogu.checkin.dto.v1.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.v1.CheckInStatusResponse;
import com.yagubogu.checkin.dto.v1.CreateCheckInRequest;
import com.yagubogu.checkin.dto.v1.FanRateResponse;
import com.yagubogu.checkin.dto.v1.StadiumCheckInCountsResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
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

    private static final double ROUND_FACTOR = 10.0;

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;
    private final StadiumRepository stadiumRepository;
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createCheckIn(final Long memberId, final CreateCheckInRequest request) {
        Game game = getGameById(request.gameId());
        Member member = getMember(memberId);
        Team team = member.getTeam();

        CheckIn checkIn = new CheckIn(game, member, team, CheckInType.LOCATION_CHECK_IN);
        checkInRepository.save(checkIn);

        applicationEventPublisher.publishEvent(new CheckInEvent(member));
        applicationEventPublisher.publishEvent(new StadiumVisitEvent(member, game.getStadium().getId()));
        applicationEventPublisher.publishEvent(new CheckInCreatedEvent(game.getDate()));
    }

    public FanRateResponse findFanRatesByGames(final long memberId, final LocalDate date) {
        Member member = getMember(memberId);
        Team myTeam = member.getTeam();

        List<FanRateGameParam> fanRatesByGames = new ArrayList<>();
        FanRateByGameParam myFanRateByGame = null;
        List<GameWithFanCountsParam> gameWithFanCounts = checkInRepository.findGamesWithFanCountsByDate(date);

        for (GameWithFanCountsParam gameWithFanCount : gameWithFanCounts) {
            Game game = gameWithFanCount.game();
            FanRateByGameParam response = createFanRateByGameResponse(gameWithFanCount);

            if (game.hasTeam(myTeam)) {
                myFanRateByGame = createFanRateByGameResponse(gameWithFanCount);
                continue;
            }
            fanRatesByGames.add(new FanRateGameParam(gameWithFanCount.totalCheckInCounts(), response));
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
        List<CheckInGameParam> checkIns = checkInRepository.findCheckInHistory(
                member,
                team,
                year,
                resultFilter,
                orderFilter
        );

        return new CheckInHistoryResponse(checkIns);
    }


    public StadiumCheckInCountsResponse findStadiumCheckInCounts(final long memberId, final int year) {
        Member member = getMember(memberId);
        List<StadiumCheckInCountParam> stadiumCheckInCounts = checkInRepository.findStadiumCheckInCounts(member,
                year);

        return new StadiumCheckInCountsResponse(stadiumCheckInCounts);
    }

    public CheckInStatusResponse findLocationCheckInStatus(final long memberId, final LocalDate date) {
        Member member = getMember(memberId);
        boolean isCheckIn = checkInRepository.existsByMemberAndGameDateAndCheckInType(
                member,
                date,
                CheckInType.LOCATION_CHECK_IN
        );

        return new CheckInStatusResponse(isCheckIn);
    }

    public List<GameWithFanRateParam> buildCheckInEventData(final LocalDate date) {
        List<GameWithFanRateParam> result = new ArrayList<>();

        List<GameWithFanCountsParam> responses = checkInRepository.findGamesWithFanCountsByDate(date);
        for (GameWithFanCountsParam response : responses) {
            Game game = response.game();
            long homeTeamCounts = response.homeTeamCheckInCounts();
            long awayTeamCounts = response.awayTeamCheckInCounts();
            long totalCounts = response.totalCheckInCounts();

            double homeTeamRate = calculateRoundRate(homeTeamCounts, totalCounts);
            double awayTeamRate = calculateRoundRate(awayTeamCounts, totalCounts);
            result.add(GameWithFanRateParam.from(game, homeTeamRate, awayTeamRate));
        }

        return result;
    }


    private Stadium getStadiumById(final long stadiumId) {
        return stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new NotFoundException("Stadium is not found"));
    }

    private Game getGameById(final long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private Game getGame(final Stadium stadium, final LocalDate date) {
        return gameRepository.findByStadiumAndDate(stadium, date)
                .orElseThrow(() -> new NotFoundException("Game is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private FanRateByGameParam createFanRateByGameResponse(final GameWithFanCountsParam gameWithFanCounts) {
        Long total = gameWithFanCounts.totalCheckInCounts();
        double homeRate = calculateRoundRate(gameWithFanCounts.homeTeamCheckInCounts(), total);
        double awayRate = calculateRoundRate(gameWithFanCounts.awayTeamCheckInCounts(), total);

        return FanRateByGameParam.from(gameWithFanCounts.game(), total, homeRate, awayRate);
    }

    private double calculateRoundRate(final Long checkInCounts, final Long total) {
        if (total == 0) {
            return 0.0;
        }

        return Math.round(((double) checkInCounts / total) * 1000) / ROUND_FACTOR;
    }
}
