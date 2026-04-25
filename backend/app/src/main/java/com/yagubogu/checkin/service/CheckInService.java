package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInImage;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.checkin.dto.*;
import com.yagubogu.checkin.dto.event.CheckInEvent;
import com.yagubogu.checkin.dto.event.StadiumVisitEvent;
import com.yagubogu.checkin.dto.v1.*;
import com.yagubogu.checkin.repository.CheckInImageRepository;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameHitterRecord;
import com.yagubogu.game.domain.GamePitcherRecord;
import com.yagubogu.game.repository.GameHitterRecordRepository;
import com.yagubogu.game.repository.GamePitcherRecordRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.S3Properties;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.stat.repository.LocationCheckInRankingRepository;
import com.yagubogu.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class CheckInService {

    private static final double ROUND_FACTOR = 10.0;

    private final CheckInRepository checkInRepository;
    private final CheckInImageRepository checkInImageRepository;
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final GameHitterRecordRepository hitterRecordRepository;
    private final GamePitcherRecordRepository pitcherRecordRepository;
    private final LocationCheckInRankingRepository locationCheckInRankingRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Transactional
    public void createCheckIn(final Long memberId, final CreateCheckInRequest request) {
        Game game = getGameById(request.gameId());
        Member member = getMember(memberId);
        Team team = member.getTeam();

        validateNotExistGameAndMember(game, member);
        saveCheckInSafely(game, member, team);
        locationCheckInRankingRepository.upsertIncrement(member.getId(), game.getDate().getYear());

        applicationEventPublisher.publishEvent(new CheckInEvent(member));
        applicationEventPublisher.publishEvent(new StadiumVisitEvent(member, game.getStadium().getId()));
        applicationEventPublisher.publishEvent(new CheckInCreatedEvent());
    }

    @Transactional
    public void deleteCheckIn(final Long memberId, final Long checkInId) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new NotFoundException("CheckIn is not found"));

        validateCheckInOwner(checkIn, memberId);

        checkInRepository.delete(checkIn);
        if (checkIn.getCheckInType() == CheckInType.LOCATION_CHECK_IN) {
            int gameYear = checkIn.getGame().getDate().getYear();
            locationCheckInRankingRepository.decrement(checkIn.getMember().getId(), gameYear);
            locationCheckInRankingRepository.deleteZeroCount(checkIn.getMember().getId(), gameYear);
        }
    }

    private void validateCheckInOwner(final CheckIn checkIn, final Long memberId) {
        if (!checkIn.getMember().getId().equals(memberId)) {
            throw new ForbiddenException("Only your own check-in can be deleted");
        }
    }

    @Transactional
    public void updateMemo(final Long memberId, final Long checkInId, final String memo) {
        Member member = getMember(memberId);
        CheckIn checkIn = getCheckInByIdAndMember(checkInId, member);
        checkIn.updateMemo(memo);
    }

    @Transactional
    public void deleteMemo(final Long memberId, final Long checkInId) {
        Member member = getMember(memberId);
        CheckIn checkIn = getCheckInByIdAndMember(checkInId, member);
        checkIn.updateMemo(null);
    }

    public CheckInMemoResponse getMemo(final Long memberId, final Long checkInId) {
        Member member = getMember(memberId);
        CheckIn checkIn = getCheckInByIdAndMember(checkInId, member);
        return new CheckInMemoResponse(checkIn.getMemo());
    }

    @Transactional
    public CheckInImageParam addImage(final Long memberId, final Long checkInId, final String imageKey) {
        Member member = getMember(memberId);
        CheckIn checkIn = getCheckInByIdAndMember(checkInId, member);
        String imageUrl = resolveImageUrl(imageKey);
        CheckInImage image = checkInImageRepository.save(new CheckInImage(checkIn, imageUrl));
        return new CheckInImageParam(image.getId(), image.getImageUrl());
    }

    @Transactional
    public void deleteImage(final Long memberId, final Long checkInId, final Long imageId) {
        Member member = getMember(memberId);
        getCheckInByIdAndMember(checkInId, member);
        CheckInImage image = checkInImageRepository.findById(imageId)
                .filter(img -> img.getCheckIn().getId().equals(checkInId))
                .orElseThrow(() -> new NotFoundException("CheckInImage is not found"));
        checkInImageRepository.delete(image);
    }

    public CheckInImagesResponse getImages(final Long memberId, final Long checkInId) {
        Member member = getMember(memberId);
        getCheckInByIdAndMember(checkInId, member);
        List<CheckInImageParam> images = checkInImageRepository.findByCheckInId(checkInId).stream()
                .map(img -> new CheckInImageParam(img.getId(), img.getImageUrl()))
                .toList();
        return new CheckInImagesResponse(images);
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
            final Integer year,
            final Integer month,
            final CheckInResultFilter resultFilter,
            final CheckInOrderFilter orderFilter
    ) {
        Member member = getMember(memberId);
        Team team = member.getTeam();
        List<CheckInGameParam> checkIns = checkInRepository.findCheckInHistory(
                member,
                team,
                year,
                month,
                resultFilter,
                orderFilter
        );

        return new CheckInHistoryResponse(checkIns);
    }

    public CheckInReviewResponse findCheckInReview(final long memberId, final long checkInId) {
        CheckIn checkIn = getCheckInByCheckInIdAndMemberId(checkInId, memberId);
        Game game = checkIn.getGame();

        List<GameHitterRecord> hitters = hitterRecordRepository.findAllByGame(game);
        List<GamePitcherRecord> pitchers = pitcherRecordRepository.findAllByGame(game);

        return CheckInReviewResponse.from(hitters, pitchers);
    }

    private CheckIn getCheckInByCheckInIdAndMemberId(final long checkInId, final long memberId) {
        return checkInRepository.findByIdAndMemberId(checkInId, memberId)
                .orElseThrow(() -> new NotFoundException("CheckIn is not found"));
    }

    public StadiumCheckInCountsResponse findStadiumCheckInCounts(final long memberId, final Integer year) {
        Member member = getMember(memberId);
        List<StadiumCheckInCountParam> stadiumCheckInCounts = checkInRepository.findStadiumCheckInCounts(member, year);

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

    private void saveCheckInSafely(final Game game, final Member member, final Team team) {
        CheckIn checkIn = new CheckIn(game, member, team, CheckInType.LOCATION_CHECK_IN, null, null);
        try {
            checkInRepository.save(checkIn);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("CheckIn is already exists");
        }
    }

    private void validateNotExistGameAndMember(final Game game, final Member member) {
        if (checkInRepository.existsByGameAndMember(game, member)) {
            throw new ConflictException("CheckIn is already exists");
        }
    }

    private CheckIn getCheckInByIdAndMember(final Long checkInId, final Member member) {
        CheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new NotFoundException("CheckIn is not found"));
        if (!checkIn.getMember().equals(member)) {
            throw new NotFoundException("CheckIn is not found");
        }
        return checkIn;
    }

    private Game getGameById(final long gameId) {
        return gameRepository.findById(gameId)
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

    private String resolveImageUrl(final String imageKey) {
        assertObjectExists(imageKey);
        return s3Properties.endpoint() + "/" + s3Properties.bucket() + "/" + imageKey;
    }

    private void assertObjectExists(final String key) {
        try {
            s3Client.headObject(r -> r.bucket(s3Properties.bucket()).key(key));
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("File does not exist in S3: " + key);
        }
    }
}
