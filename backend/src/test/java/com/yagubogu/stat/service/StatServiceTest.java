package com.yagubogu.stat.service;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class StatServiceTest {

    private StatService statService;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @BeforeEach
    void setUp() {
        statService = new StatService(checkInRepository, memberRepository, stadiumRepository);
    }

    @DisplayName("승이 1인 맴버의 통계를 계산한다.")
    @Test
    void findStatCounts_winCounts() {
        // given
        long memberId = 1L;
        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(memberId, year);

        // then
        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(actual.winCounts()).isEqualTo(5);
                    softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
                    softAssertions.assertThat(actual.loseCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(6);
                }
        );
    }

    @DisplayName("무가 1인 사용자의 통계를 계산한다")
    @Test
    void findStatCounts_drawCount() {
        // given
        long memberId = 3L;
        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(memberId, year);

        // then
        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(actual.winCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
                    softAssertions.assertThat(actual.loseCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(1);
                }
        );
    }

    @DisplayName("패가 1인 사용자의 통계를 계산한다")
    @Test
    void findStatCounts_loseCount() {
        // given
        long memberId = 2L;
        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(memberId, year);

        // then
        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(actual.winCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.drawCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.loseCounts()).isEqualTo(1);
                    softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(1);
                }
        );
    }

    @DisplayName("회원이 조회되지 않으면 NotFoundException이 발생한다")
    @Test
    void findStatCounts_notFoundMember() {
        // given
        long memberId = 99L;
        int year = 2025;

        // when & then
        assertThatThrownBy(() -> statService.findStatCounts(memberId, year))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("관리자인 경우 ForbiddenException 발생한다")
    @Test
    void findStatCounts_isAdmin() {
        // given
        long memberId = 4L;
        int year = 2025;

        // when & then
        assertThatThrownBy(() -> statService.findStatCounts(memberId, year))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("Member should not be admin");
    }

    @DisplayName("승률을 계산한다")
    @Test
    void findWinRate() {
        // given
        long memberId = 1L;
        int year = 2025;

        // when
        WinRateResponse actual = statService.findWinRate(memberId, year);

        // then
        assertThat(actual.winRate()).isEqualTo(83.3);
    }

    @DisplayName("0%가 아닌 승률이 있을 때 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_withWinRate() {
        // given
        long memberId = 1L;
        int year = 2025;

        // when
        LuckyStadiumResponse luckyStadium = statService.findLuckyStadium(memberId, year);

        // then
        assertThat(luckyStadium.shortName()).isEqualTo("챔피언스필드");
    }

    @DisplayName("모든 승률이 0%일 때 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_withOnlyZeroPercentWinRate() {
        // given
        long memberId = 2L;
        int year = 2025;

        // when
        LuckyStadiumResponse actual = statService.findLuckyStadium(memberId, year);

        // then
        assertThat(actual.shortName()).isNull();
    }

    @DisplayName("관람횟수가 0일 때 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_noCheckInCounts() {
        // given
        long memberId = 6L;
        int year = 2025;

        // when
        LuckyStadiumResponse luckyStadium = statService.findLuckyStadium(memberId, year);

        // then
        assertThat(luckyStadium.shortName()).isNull();
    }

    @DisplayName("평균 득, 실, 실책, 안타, 피안타 조회한다")
    @Test
    void findAverageStatistic() {
        // given
        long memberId = 1L;
        AverageStatisticResponse expected = new AverageStatisticResponse(
                7.9,
                6.0,
                0.3,
                10.6,
                8.3
        );

        // when
        AverageStatisticResponse actual = statService.findAverageStatistic(memberId);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.averageRuns()).isEqualTo(expected.averageRuns());
            softAssertions.assertThat(actual.averageAllowedRuns()).isEqualTo(expected.averageAllowedRuns());
            softAssertions.assertThat(actual.averageErrors()).isEqualTo(expected.averageErrors());
            softAssertions.assertThat(actual.averageHits()).isEqualTo(expected.averageHits());
            softAssertions.assertThat(actual.averageAllowedHits()).isEqualTo(expected.averageAllowedHits());
        });
    }
}
