package com.yagubogu.checkin.service;

import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.CheckInGameTeamResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateByGameResponse;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.TeamFanRateResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses.VictoryFairyRankingResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class CheckInServiceTest {

    private CheckInService checkInService;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        checkInService = new CheckInService(checkInRepository, memberRepository, stadiumRepository, gameRepository);
    }

    @DisplayName("인증을 저장한다")
    @Test
    void findOccupancyRate() {
        // given
        long memberId = 1L;
        long stadiumId = 1L;
        LocalDate date = TestFixture.getToday();

        // when & then
        assertThatCode(() -> checkInService.createCheckIn(new CreateCheckInRequest(memberId, stadiumId, date)))
                .doesNotThrowAnyException();
    }

    @DisplayName("구장을 찾을 수 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundStadium() {
        // given
        long invalidStadiumId = 999L;
        long validMemberId = 1L;
        LocalDate date = TestFixture.getToday();

        CreateCheckInRequest request = new CreateCheckInRequest(validMemberId, invalidStadiumId, date);
        // when & then
        assertThatThrownBy(() -> checkInService.createCheckIn(request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Stadium is not found");
    }

    @DisplayName("경기를 찾을 수 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundGame() {
        // given
        long validMemberId = 1L;
        long stadiumId = 1L;
        LocalDate invalidDate = TestFixture.getInvalidDate();
        CreateCheckInRequest request = new CreateCheckInRequest(validMemberId, stadiumId, invalidDate);

        // when & then
        assertThatThrownBy(() -> checkInService.createCheckIn(request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Game is not found");
    }

    @DisplayName("회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundMember() {
        // given
        long invalidStadiumId = 1L;
        long invalidMemberId = 999L;
        LocalDate date = TestFixture.getToday();
        CreateCheckInRequest request = new CreateCheckInRequest(invalidMemberId, invalidStadiumId, date);

        // when & then
        assertThatThrownBy(() -> checkInService.createCheckIn(request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("회원의 총 인증 횟수를 조회한다")
    @Test
    void findCheckInCounts() {
        // given
        long memberId = 1L;
        int year = 2025;
        int expected = 6;

        // when
        CheckInCountsResponse actual = checkInService.findCheckInCounts(memberId, year);

        // then
        assertThat(actual.checkInCounts()).isEqualTo(expected);
    }

    @DisplayName("직관 인증 내역을 모두 조회한다")
    @Test
    void findCheckInHistory_allCheckInsGivenYear() {
        // given
        long memberId = 1L;
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.ALL;

        int expectedSize = 6;

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory().size()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 인증 내역이 인증 날짜 내림차순으로 정렬되어 반환된다")
    @Test
    void findCheckInHistory_sortsByCheckInDateDescending() {
        // given
        long memberId = 1L;
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.ALL;

        List<CheckInGameResponse> expected = List.of(
                new CheckInGameResponse(1L,
                        "잠실 야구장",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 21)
                ),
                new CheckInGameResponse(2L,
                        "잠실 야구장",
                        new CheckInGameTeamResponse(1L, "기아", 5, true),
                        new CheckInGameTeamResponse(3L, "삼성", 5, false),
                        LocalDate.of(2025, 7, 20)
                ),
                new CheckInGameResponse(3L,
                        "잠실 야구장",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(3L, "삼성", 5, false),
                        LocalDate.of(2025, 7, 19)
                ),
                new CheckInGameResponse(4L,
                        "광주 KIA 챔피언스필드",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 18)
                ),
                new CheckInGameResponse(5L,
                        "광주 KIA 챔피언스필드",
                        new CheckInGameTeamResponse(3L, "삼성", 1, false),
                        new CheckInGameTeamResponse(1L, "기아", 9, true),
                        LocalDate.of(2025, 7, 17)
                ),
                new CheckInGameResponse(6L,
                        "대구 삼성라이온즈파크",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 16)
                )
        );

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory()).containsExactlyElementsOf(expected);
    }

    @DisplayName("승리 요정 랭킹 조회 - 승률, 직관 횟수, 닉네임 순 정렬되어 반환된다")
    @Test
    void findVictoryFairyRankings() {
        // given
        long memberId = 5L;
        List<VictoryFairyRankingResponse> expectedTop5Rankings = List.of(
                new VictoryFairyRankingResponse(
                        1,
                        "구구",
                        "KT",
                        100.0
                ),
                new VictoryFairyRankingResponse(
                        2,
                        "메다",
                        "LG",
                        100.0
                ),
                new VictoryFairyRankingResponse(
                        3,
                        "밍트",
                        "기아",
                        100.0
                ),
                new VictoryFairyRankingResponse(
                        4,
                        "크림",
                        "삼성",
                        100.0
                ),
                new VictoryFairyRankingResponse(
                        5,
                        "포르",
                        "기아",
                        83.3
                )
        );
        VictoryFairyRankingResponse expectedMemberRanking = new VictoryFairyRankingResponse(
                3,
                "밍트",
                "기아",
                100.0
        );

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(memberId);

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings()).containsExactlyElementsOf(expectedTop5Rankings);
                    softAssertions.assertThat(actual.myRanking().ranking())
                            .isEqualTo(expectedMemberRanking.ranking());
                    softAssertions.assertThat(actual.myRanking().nickname())
                            .isEqualTo(expectedMemberRanking.nickname());
                    softAssertions.assertThat(actual.myRanking().teamShortName())
                            .isEqualTo(expectedMemberRanking.teamShortName());
                    softAssertions.assertThat(actual.myRanking().winPercent())
                            .isEqualTo(expectedMemberRanking.winPercent());
                }
        );
    }

    @DisplayName("직관 인증 내역 중 이긴 직관 내역을 모두 조회한다")
    @Test
    void findCheckInWinHistory_allCheckInWinsGivenYear() {
        // given
        long memberId = 1L;
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.WIN;
        int expectedSize = 5;

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory().size()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 인증 내역 중 이긴 내역만 필터링되어 인증 날짜 내림차순으로 반환된다")
    @Test
    void findCheckInWinHistory_returnsOnlyWinsSortedByDateDescending() {
        // given
        long memberId = 1L;
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.WIN;

        List<CheckInGameResponse> expected = List.of(
                new CheckInGameResponse(1L,
                        "잠실 야구장",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 21)
                ),
                new CheckInGameResponse(3L,
                        "잠실 야구장",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(3L, "삼성", 5, false),
                        LocalDate.of(2025, 7, 19)
                ),
                new CheckInGameResponse(4L,
                        "광주 KIA 챔피언스필드",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 18)
                ),
                new CheckInGameResponse(5L,
                        "광주 KIA 챔피언스필드",
                        new CheckInGameTeamResponse(3L, "삼성", 1, false),
                        new CheckInGameTeamResponse(1L, "기아", 9, true),
                        LocalDate.of(2025, 7, 17)
                ),
                new CheckInGameResponse(6L,
                        "대구 삼성라이온즈파크",
                        new CheckInGameTeamResponse(1L, "기아", 10, true),
                        new CheckInGameTeamResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 16)
                )
        );

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory()).containsExactlyElementsOf(expected);
    }

    @DisplayName("오늘 경기 구장별 팬 점유율 조회 – 내 팀 경기 처음, 나머지 관중 수 많은 순 정렬")
    @Test
    void findFanRatesByGames() {
        // given
        long memberId = 1L;
        LocalDate today = TestFixture.getToday();
        // 내 팀 포함 경기(기아 vs 롯데) → 관중 수 기준 정렬된 경기 (LG vs KT, 삼성 vs 두산)
        FanRateResponse expected = new FanRateResponse(
                List.of(
                        new FanRateByGameResponse(
                                3L,
                                new TeamFanRateResponse("기아", "HT", 66.7),
                                new TeamFanRateResponse("롯데", "LT", 33.3)
                        ),
                        new FanRateByGameResponse(
                                4L,
                                new TeamFanRateResponse("LG", "LG", 75.0),
                                new TeamFanRateResponse("KT", "KT", 25.0)
                        ),
                        new FanRateByGameResponse(
                                2L,
                                new TeamFanRateResponse("삼성", "SS", 50.0),
                                new TeamFanRateResponse("두산", "OB", 50.0)
                        )
                )
        );

        // when
        FanRateResponse actual = checkInService.findFanRatesByGames(memberId, today);

        // then
        assertThat(actual.fanRateByGames()).containsExactlyElementsOf(expected.fanRateByGames());
    }
}
