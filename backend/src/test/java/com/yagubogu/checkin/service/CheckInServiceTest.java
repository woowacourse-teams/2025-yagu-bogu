package com.yagubogu.checkin.service;

import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.dto.TeamInfoResponse;
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

    @DisplayName("인증을 저장한다.")
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

    @DisplayName("구장을 찾을 수 없으면 예외가 발생한다.")
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

    @DisplayName("경기를 찾을 수 없으면 예외가 발생한다.")
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

    @DisplayName("회원을 찾을 수 없으면 예외가 발생한다.")
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

    @DisplayName("당해 년도 직관 내역을 모두 조회한다")
    @Test
    void findCheckInHistory_allCheckInsGivenYear() {
        // given
        long memberId = 1L;
        int year = 2025;

        int expectedSize = 6;

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year);

        // then
        assertThat(actual.checkInHistory().size()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 내역 조회 결과가 날짜 순으로 정렬되어 반환된다")
    @Test
    void findCheckInHistory_sortsByCheckInDateDescending() {
        // given
        long memberId = 1L;
        int year = 2025;

        List<CheckInResponse> expected = List.of(
                new CheckInResponse(1L,
                        "잠실 야구장",
                        new TeamInfoResponse(1L, "기아", 10, true),
                        new TeamInfoResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 21)
                ),
                new CheckInResponse(2L,
                        "잠실 야구장",
                        new TeamInfoResponse(1L, "기아", 5, true),
                        new TeamInfoResponse(3L, "삼성", 5, false),
                        LocalDate.of(2025, 7, 20)
                ),
                new CheckInResponse(3L,
                        "잠실 야구장",
                        new TeamInfoResponse(1L, "기아", 10, true),
                        new TeamInfoResponse(3L, "삼성", 5, false),
                        LocalDate.of(2025, 7, 19)
                ),
                new CheckInResponse(4L,
                        "광주 KIA 챔피언스필드",
                        new TeamInfoResponse(1L, "기아", 10, true),
                        new TeamInfoResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 18)
                ),
                new CheckInResponse(5L,
                        "광주 KIA 챔피언스필드",
                        new TeamInfoResponse(3L, "삼성", 1, false),
                        new TeamInfoResponse(1L, "기아", 9, true),
                        LocalDate.of(2025, 7, 17)
                ),
                new CheckInResponse(6L,
                        "대구 삼성라이온즈파크",
                        new TeamInfoResponse(1L, "기아", 10, true),
                        new TeamInfoResponse(2L, "롯데", 9, false),
                        LocalDate.of(2025, 7, 16)
                )
        );

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year);

        // then
        assertThat(actual.checkInHistory()).containsExactlyElementsOf(expected);
    }
}
