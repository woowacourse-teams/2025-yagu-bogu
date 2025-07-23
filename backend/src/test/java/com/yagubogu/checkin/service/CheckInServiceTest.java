package com.yagubogu.checkin.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.time.LocalDate;
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
}
