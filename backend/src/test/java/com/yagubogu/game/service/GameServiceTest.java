package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.dto.GamesResponse;
import com.yagubogu.game.dto.GamesResponse.GameResponse;
import com.yagubogu.game.dto.GamesResponse.StadiumInfoResponse;
import com.yagubogu.game.dto.GamesResponse.TeamInfoResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.UnprocessableEntityException;
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
class GameServiceTest {

    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository);
    }

    @DisplayName("오늘 경기하는 모든 구장, 팀 조회한다")
    @Test
    void findGamesByDate() {
        // given
        LocalDate date = TestFixture.getToday();
        List<GameResponse> expected = List.of(
                new GameResponse(
                        new StadiumInfoResponse(1L, "잠실 야구장"),
                        new TeamInfoResponse(1L, "기아 타이거즈", "HT"),
                        new TeamInfoResponse(2L, "롯데 자이언츠", "LT")
                ),
                new GameResponse(
                        new StadiumInfoResponse(2L, "고척 스카이돔"),
                        new TeamInfoResponse(3L, "삼성 라이온즈", "SS"),
                        new TeamInfoResponse(4L, "두산 베어스", "OB")
                ),
                new GameResponse(
                        new StadiumInfoResponse(3L, "인천 SSG 랜더스필드"),
                        new TeamInfoResponse(5L, "LG 트윈스", "LG"),
                        new TeamInfoResponse(6L, "KT 위즈", "KT")
                )
        );

        // when
        GamesResponse actual = gameService.findGamesByDate(date);

        // then
        assertThat(actual.games()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("예외: 미래 날짜를 조회하려고 하면 예외가 발생한다")
    @Test
    void findGamesByDate_WhenDateIsInFuture() {
        // given
        LocalDate invalidDate = LocalDate.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> gameService.findGamesByDate(invalidDate))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("Cannot retrieve games for future dates");
    }
}
