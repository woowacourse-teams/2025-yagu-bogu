package com.yagubogu.game.service;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.dto.GameResponse;
import com.yagubogu.game.dto.GameWithCheckIn;
import com.yagubogu.game.dto.StadiumByGame;
import com.yagubogu.game.dto.TeamByGame;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class GameServiceTest {

    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository, memberRepository);
    }

    @DisplayName("오늘 경기하는 모든 구장, 팀, 인증 횟수, 내 인증 여부를 조회한다")
    @Test
    void findGamesByDate() {
        // given
        LocalDate date = TestFixture.getToday();
        long memberId = 1L;
        List<GameWithCheckIn> expected = List.of(
                new GameWithCheckIn(
                        3L,
                        true,
                        new StadiumByGame(1L, "잠실 야구장"),
                        new TeamByGame(1L, "기아", "HT"),
                        new TeamByGame(2L, "롯데", "LT")),
                new GameWithCheckIn(
                        4L,
                        true,
                        new StadiumByGame(2L, "고척 스카이돔"),
                        new TeamByGame(3L, "삼성", "SS"),
                        new TeamByGame(4L, "두산", "OB")),
                new GameWithCheckIn(
                        4L,
                        false,
                        new StadiumByGame(3L, "인천 SSG 랜더스필드"),
                        new TeamByGame(5L, "LG", "LG"),
                        new TeamByGame(6L, "KT", "KT"))
        );

        // when
        GameResponse actual = gameService.findGamesByDate(date, memberId);

        // then
        assertThat(actual.games()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("예외: 미래 날짜를 조회하려고 하면 예외가 발생한다")
    @Test
    void findGamesByDate_WhenDateIsInFuture() {
        // given
        long memberId = 1L;
        LocalDate invalidDate = LocalDate.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> gameService.findGamesByDate(invalidDate, memberId))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("Cannot retrieve games for future dates");
    }

    @DisplayName("예외: 오늘 경기하는 모든 구장, 팀, 인증횟수, 내 인증 여부를 조회할 때 회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void findGamesByDate_notFoundMember() {
        // given
        long invalidMemberId = 999L;
        LocalDate invalidDate = TestFixture.getToday();

        // when & then
        assertThatThrownBy(() -> gameService.findGamesByDate(invalidDate, invalidMemberId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }
}
