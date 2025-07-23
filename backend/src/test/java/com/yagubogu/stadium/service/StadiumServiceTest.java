package com.yagubogu.stadium.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse.TeamOccupancyRate;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
import java.util.List;
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

    @DisplayName("구장별 팬 점유율을 조회한다")
    @Test
    void findOccupancyRate() {
        // given
        long stadiumId = 1L;
        LocalDate today = TestFixture.getToday();

        // when
        TeamOccupancyRatesResponse response = stadiumService.findOccupancyRate(stadiumId, today);

        // then
        assertSoftly(
                softAssertions -> {
                    List<TeamOccupancyRate> teams = response.teams();
                    TeamOccupancyRate first = teams.getFirst();
                    softAssertions.assertThat(first.name()).isEqualTo("기아");
                    softAssertions.assertThat(first.occupancyRate()).isEqualTo(66.7);

                    TeamOccupancyRate second = teams.get(1);
                    softAssertions.assertThat(second.name()).isEqualTo("롯데");
                    softAssertions.assertThat(second.occupancyRate()).isEqualTo(33.3);
                }
        );
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
