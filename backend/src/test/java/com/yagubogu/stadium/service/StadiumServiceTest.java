package com.yagubogu.stadium.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse;
import com.yagubogu.stadium.dto.TeamOccupancyRatesResponse.TeamOccupancyRate;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class StadiumServiceTest {

    private StadiumService stadiumService;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(gameRepository, checkInRepository);
    }

    @DisplayName("구장별 팬 점유율을 조회한다")
    @Test
    void findOccupancyRate() {
        // given
        long stadiumId = 1L;
        LocalDate today = TestFixture.getLocalDate();

        // when
        TeamOccupancyRatesResponse response = stadiumService.findOccupancyRate(stadiumId, today);

        // then
        SoftAssertions.assertSoftly(
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

    @DisplayName("구장에 오늘 경기가 없으면 예외가 발생한다.")
    @ParameterizedTest
    @CsvSource({"999, 2025-07-21", "1, 3000-05-05"})
    void findOccupancyRate_notTodayGameInStadium(long stadiumId, LocalDate today) {
        // when & then
        assertThatThrownBy(() -> stadiumService.findOccupancyRate(stadiumId, today))
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
