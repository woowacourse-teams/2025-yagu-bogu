package com.yagubogu.game.service;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.dto.GamesResponse;
import com.yagubogu.game.dto.GamesResponse.GameResponse;
import com.yagubogu.game.dto.GamesResponse.StadiumInfoResponse;
import com.yagubogu.game.dto.GamesResponse.TeamInfoResponse;
import com.yagubogu.game.dto.KboClientResponse;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboClient;
import com.yagubogu.global.exception.ClientException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class GameServiceTest {

    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Mock
    private KboClient kboClient;

    @BeforeEach
    void setUp() {
        gameService = new GameService(kboClient, gameRepository, teamRepository, stadiumRepository);
    }

    @DisplayName("경기 목록을 성공적으로 가져와서 저장한다")
    @Test
    void fetchGameList() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721OBLG0", "2025-07-21", 0L, "18:30",
                "잠실", "기아", "두산", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when
        gameService.fetchGameList(today);

        // then
        assertThat(gameRepository.findAll()
                .stream()
                .filter(game ->
                        game.getGameCode().equals(gameItem.gameCode()))
                .findFirst())
                .isPresent();
    }

    @DisplayName("예외 : 경기장을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameList_stadiumNotFound() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", "2025-07-21", 0L, "18:30",
                "존재하지않는경기장", "한화", "삼성", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameService.fetchGameList(today))
                .isInstanceOf(ClientException.class)
                .hasMessage("Stadium name match failed: 존재하지않는경기장");
    }

    @DisplayName("예외 : 홈팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameList_homeTeamNotFound() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", "2025-07-21", 0L, "18:30",
                "잠실", "존재하지않는원정팀", "삼성", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameService.fetchGameList(today))
                .isInstanceOf(ClientException.class)
                .hasMessage("Team code match failed: 존재하지않는원정팀");
    }

    @DisplayName("예외 : 원정팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameList_awayTeamNotFound() {
        // given
        LocalDate today = TestFixture.getToday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", "2025-07-21", 0L, "18:30",
                "잠실", "한화", "존재하지않는원정팀", "정상경기", "", "정규시즌", "5", "3"
        );
        KboClientResponse response = new KboClientResponse(List.of(gameItem), "100", "success");

        given(kboClient.fetchGame(today)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameService.fetchGameList(today))
                .isInstanceOf(ClientException.class)
                .hasMessage("Team code match failed: 존재하지않는원정팀");
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
