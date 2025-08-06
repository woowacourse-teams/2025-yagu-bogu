package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.dto.KboGameResultResponse.KboScoreBoardResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameResultClient;
import com.yagubogu.game.service.client.KboGameSyncClient;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DataJpaTest
class GameScheduleSyncServiceTest {

    private GameScheduleSyncService gameScheduleSyncService;
    private GameResultSyncService gameResultSyncService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Mock
    private KboGameSyncClient kboGameSyncClient;

    @Mock
    private KboGameResultClient kboGameResultClient;

    @BeforeEach
    void setUp() {
        gameScheduleSyncService = new GameScheduleSyncService(kboGameSyncClient, gameRepository, teamRepository,
                stadiumRepository);
        gameResultSyncService = new GameResultSyncService(kboGameSyncClient, kboGameResultClient, gameRepository);
    }

    @DisplayName("경기 목록을 성공적으로 가져와서 저장한다")
    @Test
    void syncGameSchedule() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721OBLG3", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when
        gameScheduleSyncService.syncGameSchedule(yesterday);

        // then
        assertThat(gameRepository.findByGameCode(gameItem.gameCode())).isPresent();
    }

    @DisplayName("예외: 경기장을 찾을 수 없으면 예외가 발생한다")
    @Test
    void syncGameSchedule_stadiumNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "존재하지않는경기장", "HH", "SS", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameScheduleSyncService.syncGameSchedule(yesterday))
                .isInstanceOf(GameSyncException.class)
                .hasMessage("Stadium name match failed: 존재하지않는경기장");
    }

    @DisplayName("예외 : 홈팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void syncGameSchedule_homeTeamNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "잠실", "존재하지않는원정팀", "SS", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameScheduleSyncService.syncGameSchedule(yesterday))
                .isInstanceOf(GameSyncException.class)
                .hasMessage("Team code match failed: 존재하지않는원정팀");
    }

    @DisplayName("예외 : 원정팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void syncGameSchedule_awayTeamNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "잠실", "HH", "존재하지않는원정팀", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameScheduleSyncService.syncGameSchedule(yesterday))
                .isInstanceOf(GameSyncException.class)
                .hasMessage("Team code match failed: 존재하지않는원정팀");
    }

    @DisplayName("경기 결과를 성공적으로 가져와서 저장한다")
    @Test
    void syncGameResult() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String gameCode = "20250721OBLG0";

        KboGameResponse kboGameResponse = new KboGameResponse(
                gameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.COMPLETED);
        given(kboGameSyncClient.fetchGames(yesterday))
                .willReturn(new KboGamesResponse(List.of(kboGameResponse), "100", "success"));

        KboScoreBoardResponse home = new KboScoreBoardResponse(5, 8, 1, 3);
        KboScoreBoardResponse away = new KboScoreBoardResponse(3, 6, 2, 4);
        given(kboGameResultClient.fetchGameResult(any(Game.class)))
                .willReturn(new KboGameResultResponse("100", "success", home, away));

        ScoreBoard homeScoreBoardExpected = home.toScoreBoard();
        ScoreBoard awayScoreBoardExpected = away.toScoreBoard();

        // when
        gameResultSyncService.syncGameResult(yesterday);

        // then
        Game game = gameRepository.findByGameCode(gameCode).orElseThrow();
        SoftAssertions.assertSoftly((softAssertions -> {
            softAssertions.assertThat(game.getGameState()).isEqualTo(GameState.COMPLETED);
            softAssertions.assertThat(game.getHomeScoreBoard()).isEqualTo(homeScoreBoardExpected);
            softAssertions.assertThat(game.getAwayScoreBoard()).isEqualTo(awayScoreBoardExpected);
        }));
    }

    @DisplayName("LIVE 상태 경기는 스코어보드를 업데이트하지 않는다")
    @Test
    void syncGameResult_gameNotCompleted() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String gameCode = "20250721LTSS0";

        KboGameResponse kboGameResponse = new KboGameResponse(
                gameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.LIVE);
        given(kboGameSyncClient.fetchGames(yesterday))
                .willReturn(new KboGamesResponse(List.of(kboGameResponse), "100", "success"));

        // when
        gameResultSyncService.syncGameResult(yesterday);

        // then
        Game game = gameRepository.findByGameCode(gameCode).orElseThrow();
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(game.getGameState()).isEqualTo(GameState.LIVE);
            softAssertions.assertThat(game.getHomeScore()).isNull();
            softAssertions.assertThat(game.getAwayScore()).isNull();
        });
    }

    @DisplayName("CANCELED 상태 경기는 스코어보드를 업데이트하지 않는다")
    @Test
    void syncGameResult_gameCanceled() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String gameCode = "20250721LTSS0";

        KboGameResponse kboGameResponse = new KboGameResponse(
                gameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.CANCELED);
        given(kboGameSyncClient.fetchGames(yesterday))
                .willReturn(new KboGamesResponse(List.of(kboGameResponse), "100", "success"));

        // when
        gameResultSyncService.syncGameResult(yesterday);

        // then
        Game game = gameRepository.findByGameCode(gameCode).orElseThrow();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(game.getGameState()).isEqualTo(GameState.CANCELED);
            soft.assertThat(game.getHomeScore()).isNull();
            soft.assertThat(game.getAwayScore()).isNull();
        });
    }

    @DisplayName("DB에 존재하지 않는 게임 코드는 건너뛴다")
    @Test
    void syncGameResult_gameNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String unknownGameCode = "20250721XXXX0";

        KboGameResponse kboGameResponse = new KboGameResponse(
                unknownGameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.COMPLETED);
        given(kboGameSyncClient.fetchGames(yesterday))
                .willReturn(new KboGamesResponse(List.of(kboGameResponse), "100", "success"));

        // when
        gameResultSyncService.syncGameResult(yesterday);

        // then
        assertThat(gameRepository.findByGameCode(unknownGameCode)).isEmpty();
    }
}
