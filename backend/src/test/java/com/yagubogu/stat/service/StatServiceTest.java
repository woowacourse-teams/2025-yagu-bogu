package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import org.assertj.core.api.SoftAssertions;
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
class StatServiceTest {

    private StatService statService;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        statService = new StatService(checkInRepository, memberRepository);
    }

    @DisplayName("승이 1인 맴버의 통계를 계산한다.")
    @Test
    void findStatCounts_winCounts() {
        // given
        long memberId = 1L;
        int year = 2025;

        // when
        StatCountsResponse response = statService.findStatCounts(memberId, year);

        // then
        SoftAssertions.assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(response.winCounts()).isEqualTo(2);
                    softAssertions.assertThat(response.drawCounts()).isEqualTo(1);
                    softAssertions.assertThat(response.loseCounts()).isEqualTo(0);
                    softAssertions.assertThat(response.favoriteCheckInCounts()).isEqualTo(3);
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
        StatCountsResponse response = statService.findStatCounts(memberId, year);

        // then
        SoftAssertions.assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(response.winCounts()).isEqualTo(0);
                    softAssertions.assertThat(response.drawCounts()).isEqualTo(1);
                    softAssertions.assertThat(response.loseCounts()).isEqualTo(0);
                    softAssertions.assertThat(response.favoriteCheckInCounts()).isEqualTo(1);
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
        StatCountsResponse response = statService.findStatCounts(memberId, year);

        // then
        SoftAssertions.assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(response.winCounts()).isEqualTo(0);
                    softAssertions.assertThat(response.drawCounts()).isEqualTo(0);
                    softAssertions.assertThat(response.loseCounts()).isEqualTo(1);
                    softAssertions.assertThat(response.favoriteCheckInCounts()).isEqualTo(1);
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
        WinRateResponse response = statService.findWinRate(memberId, year);

        // then
        assertThat(response.winRate()).isEqualTo(66.7);
    }
}
