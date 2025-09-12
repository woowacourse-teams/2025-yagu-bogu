package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameResultClient;
import com.yagubogu.game.service.client.KboGameSyncClient;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import(AuthTestConfig.class)
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

    @Autowired
    private GameFactory gameFactory;

    @Mock
    private KboGameSyncClient kboGameSyncClient;

    @Mock
    private KboGameResultClient kboGameResultClient;

    @BeforeEach
    void setUp() {
        gameScheduleSyncService = new GameScheduleSyncService(kboGameSyncClient, gameRepository, teamRepository,
                stadiumRepository);
        gameResultSyncService = new GameResultSyncService(kboGameResultClient, gameRepository);
    }

    @DisplayName("경기 목록을 성공적으로 가져와서 저장한다")
    @Test
    void fetchGameSchedule() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721OBLG3", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when
        gameScheduleSyncService.fetchGameSchedule(yesterday);

        // then
        assertThat(gameRepository.findByGameCode(gameItem.gameCode())).isPresent();
    }

    @DisplayName("예외: 경기장을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameSchedule_stadiumNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "존재하지않는경기장", "HH", "SS", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameScheduleSyncService.fetchGameSchedule(yesterday))
                .isInstanceOf(GameSyncException.class)
                .hasMessage("Stadium name match failed: 존재하지않는경기장");
    }

    @DisplayName("예외 : 홈팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameSchedule_homeTeamNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "잠실", "존재하지않는원정팀", "SS", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameScheduleSyncService.fetchGameSchedule(yesterday))
                .isInstanceOf(GameSyncException.class)
                .hasMessage("Team code match failed: 존재하지않는원정팀");
    }

    @DisplayName("예외 : 원정팀을 찾을 수 없으면 예외가 발생한다")
    @Test
    void fetchGameSchedule_awayTeamNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        KboGameResponse gameItem = new KboGameResponse(
                "20250721SSHH0", TestFixture.getToday(), 0, LocalTime.of(18, 30),
                "잠실", "HH", "존재하지않는원정팀", GameState.COMPLETED);
        KboGamesResponse response = new KboGamesResponse(List.of(gameItem), "100", "success");

        given(kboGameSyncClient.fetchGames(yesterday)).willReturn(response);

        // when & then
        assertThatThrownBy(() -> gameScheduleSyncService.fetchGameSchedule(yesterday))
                .isInstanceOf(GameSyncException.class)
                .hasMessage("Team code match failed: 존재하지않는원정팀");
    }

    @DisplayName("경기 결과를 성공적으로 가져와서 저장한다")
    @Test
    void syncGameResult() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String gameCode = "20250721OBLG0";
        Game game = makeGame(yesterday, "OB", "HT", "잠실구장", gameCode);

        KboGameResponse response = new KboGameResponse(
                gameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.COMPLETED);

        ScoreBoard homeScoreBoard = new ScoreBoard(5, 8, 1, 3,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-"));
        ScoreBoard awayScoreBoard = new ScoreBoard(3, 6, 2, 4,
                List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-", "-"));
        String homePitcher = "이포라";
        String awayPitcher = "포라리";

        KboGameResultResponse mockGameResult = new KboGameResultResponse(
                homeScoreBoard,
                awayScoreBoard,
                homePitcher,
                awayPitcher
        );
        given(kboGameResultClient.fetchGameResult(any(Game.class)))
                .willReturn(mockGameResult);

        ScoreBoard homeScoreBoardExpected = mockGameResult.homeScoreBoard();
        ScoreBoard awayScoreBoardExpected = mockGameResult.awayScoreBoard();

        // when
        gameResultSyncService.updateGameDetails(gameCode, response);

        // then
        assertSoftly((softAssertions -> {
            softAssertions.assertThat(game.getGameState()).isEqualTo(GameState.COMPLETED);
            softAssertions.assertThat(game.getHomeScoreBoard()).isEqualTo(homeScoreBoardExpected);
            softAssertions.assertThat(game.getAwayScoreBoard()).isEqualTo(awayScoreBoardExpected);
            softAssertions.assertThat(game.getHomePitcher()).isEqualTo(homePitcher);
            softAssertions.assertThat(game.getAwayPitcher()).isEqualTo(awayPitcher);
        }));
    }

    @DisplayName("LIVE 상태 경기는 스코어보드를 업데이트한다")
    @Test
    void syncGameResult_gameNotCompleted() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String gameCode = "20250721LTSS0";
        Game game = makeGame(yesterday, "OB", "HT", "잠실구장", gameCode);

        KboGameResponse response = new KboGameResponse(
                gameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.LIVE);

        ScoreBoard home = new ScoreBoard(1, 1, 0, 0, List.of("1"));
        ScoreBoard away = new ScoreBoard(0, 1, 0, 0, List.of("0"));
        String homePitcher = "HP";
        String awayPitcher = "AP";

        given(kboGameResultClient.fetchGameResult(any(Game.class)))
                .willReturn(new KboGameResultResponse(home, away, homePitcher, awayPitcher));

        // when
        gameResultSyncService.updateGameDetails(gameCode, response);

        // then
        assertSoftly(soft -> {
            soft.assertThat(game.getGameState()).isEqualTo(GameState.LIVE);
            soft.assertThat(game.getHomeScore()).isEqualTo(home.getRuns());
            soft.assertThat(game.getAwayScore()).isEqualTo(away.getRuns());
            soft.assertThat(game.getHomeScoreBoard().getRuns()).isEqualTo(1);
            soft.assertThat(game.getAwayScoreBoard().getRuns()).isEqualTo(0);
            soft.assertThat(game.getHomePitcher()).isEqualTo(homePitcher);
            soft.assertThat(game.getAwayPitcher()).isEqualTo(awayPitcher);
        });
    }

    @DisplayName("CANCELED 상태 경기는 스코어보드를 업데이트하지 않는다")
    @Test
    void syncGameResult_gameCanceled() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String gameCode = "20250721LTSS0";
        Game game = makeGame(yesterday, "OB", "HT", "잠실구장", gameCode);

        KboGameResponse response = new KboGameResponse(
                gameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.CANCELED);
        given(kboGameResultClient.fetchGameResult(any(Game.class)))
                .willReturn(new KboGameResultResponse(
                        new ScoreBoard(1,1,0,0, List.of("1")),
                        new ScoreBoard(0,1,0,0, List.of("0")),
                        "HP","AP"
                ));

        // when
        gameResultSyncService.updateGameDetails(gameCode, response);

        // then
        assertSoftly(soft -> {
            soft.assertThat(game.getGameState()).isEqualTo(GameState.CANCELED);
            soft.assertThat(game.getHomeScore()).isNull();
            soft.assertThat(game.getAwayScore()).isNull();
        });
    }

    @DisplayName("DB에 존재하지 않는 게임 코드면 NotFoundException 발생")
    @Test
    void syncGameResult_gameNotFound() {
        // given
        LocalDate yesterday = TestFixture.getYesterday();
        String unknownGameCode = "20250721XXXX0";

        KboGameResponse response = new KboGameResponse(
                unknownGameCode, yesterday, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.COMPLETED);

        // when & then
        assertThatThrownBy(() -> gameResultSyncService.updateGameDetails(unknownGameCode, response))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Game not found: " + unknownGameCode);
    }

    private Game makeGame(
            LocalDate date,
            String homeCode,
            String awayCode,
            String stadiumShortName,
            String gameCode
    ) {
        Team homeTeam = getTeamByCode(homeCode);
        Team awayTeam = getTeamByCode(awayCode);
        Stadium stadium = stadiumRepository.findByShortName(stadiumShortName).orElseThrow();

        return gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(date)
                .gameCode(gameCode)
        );
    }

    private Team getTeamByCode(String code) {
        return teamRepository.findByTeamCode(code).orElseThrow();
    }
}
