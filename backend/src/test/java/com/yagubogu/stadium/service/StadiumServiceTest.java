package com.yagubogu.stadium.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse.TeamOccupancyRate;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class StadiumServiceTest {

    private StadiumService stadiumService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(gameRepository, checkInRepository, stadiumRepository);
    }

    @DisplayName("전체 구장 목록을 조회한다")
    @Test
    void findAllStadiums() {
        // given
        List<StadiumResponse> expected = List.of(
                new StadiumResponse(1L, "잠실 야구장", "잠실구장", "잠실", 37.512192, 127.072055),
                new StadiumResponse(2L, "고척 스카이돔", "고척돔", "고척", 37.498191, 126.867073),
                new StadiumResponse(3L, "인천 SSG 랜더스필드", "랜더스필드", "인천", 37.437196, 126.693294),
                new StadiumResponse(4L, "대전 한화생명 볼파크", "볼파크", "대전", 36.316589, 127.431211),
                new StadiumResponse(5L, "광주 KIA 챔피언스필드", "챔피언스필드", "광주", 35.168282, 126.889138),
                new StadiumResponse(6L, "대구 삼성라이온즈파크", "라이온즈파크", "대구", 35.841318, 128.681559),
                new StadiumResponse(7L, "창원 NC파크", "엔씨파크", "창원", 35.222754, 128.582251),
                new StadiumResponse(8L, "수원 KT위즈파크", "위즈파크", "수원", 37.299977, 127.009690),
                new StadiumResponse(9L, "부산 사직야구장", "사직구장", "부산", 35.194146, 129.061497)
        );

        // when
        StadiumsResponse actual = stadiumService.findAll();

        // then
        Assertions.assertThat(actual.stadiums()).isEqualTo(expected);
    }

    @DisplayName("구장별 팬 점유율을 조회할 때 점유율이 높은 순으로 정렬된다")
    @Test
    void findOccupancyRateInDesc() {
        // given
        long stadiumId = 1L;
        LocalDate today = TestFixture.getToday();
        List<TeamOccupancyRate> expected = List.of(
                new TeamOccupancyRate(1L, "기아", 66.7),
                new TeamOccupancyRate(2L, "롯데", 33.3)
        );

        // when
        TeamOccupancyRatesResponse response = stadiumService.findOccupancyRate(stadiumId, today);
        List<TeamOccupancyRate> actual = response.teams();

        // then
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    @DisplayName("구장을 찾을 수 없으면 없으면 예외가 발생한다.")
    @Test
    void findOccupancyRate_notFoundStadium() {
        // given
        long stadiumId = 999L;
        LocalDate date = LocalDate.of(2025, 7, 21);

        // when & then
        assertThatThrownBy(() -> stadiumService.findOccupancyRate(stadiumId, date))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Stadium is not found");
    }

    @DisplayName("구장에 오늘 경기가 없으면 예외가 발생한다.")
    @Test
    void findOccupancyRate_notTodayGameInStadium() {
        long stadiumId = 1L;
        LocalDate date = TestFixture.getInvalidDate();
        // when & then
        assertThatThrownBy(() -> stadiumService.findOccupancyRate(stadiumId, date))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Game is not found");
    }

    @DisplayName("인증한 회원이 0명이면 빈 리스트를 반환한다.")
    @Test
    void findOccupancyRate_noPerson() {
        // given
        long stadiumId = 1L;
        LocalDate date = LocalDate.of(2024, 5, 5);

        // when
        TeamOccupancyRatesResponse response = stadiumService.findOccupancyRate(stadiumId, date);

        // then
        assertThat(response.teams()).isEqualTo(List.of());
    }

}
