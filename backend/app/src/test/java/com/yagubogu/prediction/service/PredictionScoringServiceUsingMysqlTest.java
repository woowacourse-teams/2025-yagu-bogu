package com.yagubogu.prediction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.dto.WeeklyScoreParam;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class PredictionScoringServiceUsingMysqlTest extends ServiceUsingMysqlTestBase {

    @Autowired
    private PredictionScoringService predictionScoringService;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private GamePredictionRepository gamePredictionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    private Team homeTeam, awayTeam;
    private Stadium stadium;

    @BeforeEach
    void setUp() {
        homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        awayTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
    }

    @DisplayName("경기가 종료되면 승리 예측은 WON, 패배 예측은 LOST로 채점된다")
    @Test
    void scorePendingGames_marksWonAndLost() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(LocalDate.of(2025, 7, 21))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        Member homePicker = memberFactory.save(b -> b.team(homeTeam));
        Member awayPicker = memberFactory.save(b -> b.team(awayTeam));

        GamePrediction wonPrediction = gamePredictionRepository.save(
                new GamePrediction(homePicker, game, PredictionPick.HOME));
        GamePrediction lostPrediction = gamePredictionRepository.save(
                new GamePrediction(awayPicker, game, PredictionPick.AWAY));

        // when
        predictionScoringService.scorePendingGames();

        // then
        GamePrediction actualWon = gamePredictionRepository.findById(wonPrediction.getId()).orElseThrow();
        GamePrediction actualLost = gamePredictionRepository.findById(lostPrediction.getId()).orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actualWon.getStatus()).isEqualTo(PredictionStatus.WON);
            softAssertions.assertThat(actualLost.getStatus()).isEqualTo(PredictionStatus.LOST);
        });
    }

    @DisplayName("경기가 취소되면 예측은 VOID로 채점된다")
    @Test
    void scorePendingGames_marksVoid() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(LocalDate.of(2025, 7, 21))
                .gameState(GameState.CANCELED));

        Member member = memberFactory.save(b -> b.team(homeTeam));
        GamePrediction prediction = gamePredictionRepository.save(
                new GamePrediction(member, game, PredictionPick.HOME));

        // when
        predictionScoringService.scorePendingGames();

        // then
        GamePrediction actual = gamePredictionRepository.findById(prediction.getId()).orElseThrow();
        assertThat(actual.getStatus()).isEqualTo(PredictionStatus.VOID);
    }

    @DisplayName("아직 종료되지 않은 경기의 예측은 채점하지 않는다")
    @Test
    void scorePendingGames_ignoresScheduledGames() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(LocalDate.of(2025, 7, 21))
                .gameState(GameState.SCHEDULED));

        Member member = memberFactory.save(b -> b.team(homeTeam));
        GamePrediction prediction = gamePredictionRepository.save(
                new GamePrediction(member, game, PredictionPick.HOME));

        // when
        predictionScoringService.scorePendingGames();

        // then
        GamePrediction actual = gamePredictionRepository.findById(prediction.getId()).orElseThrow();
        assertThat(actual.getStatus()).isEqualTo(PredictionStatus.SUBMITTED);
    }

    @DisplayName("두 번 실행해도 채점 결과는 그대로 유지된다")
    @Test
    void scorePendingGames_idempotent() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(LocalDate.of(2025, 7, 21))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        Member member = memberFactory.save(b -> b.team(homeTeam));
        GamePrediction prediction = gamePredictionRepository.save(
                new GamePrediction(member, game, PredictionPick.HOME));

        // when
        predictionScoringService.scorePendingGames();
        predictionScoringService.scorePendingGames();

        // then
        GamePrediction actual = gamePredictionRepository.findById(prediction.getId()).orElseThrow();
        assertThat(actual.getStatus()).isEqualTo(PredictionStatus.WON);
    }

    @DisplayName("주간 점수는 그 주에 WON한 예측 개수로 회원별 집계된다")
    @Test
    void findWeeklyScores_countsWonPerMember() {
        // given
        LocalDate monday = LocalDate.of(2025, 7, 21);
        LocalDate sunday = LocalDate.of(2025, 7, 27);

        Game game1 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        Game game2 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(sunday)
                .homeScore(2).awayScore(1)
                .gameState(GameState.COMPLETED));
        Game gameOutsideWeek = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday.minusDays(1))
                .homeScore(4).awayScore(0)
                .gameState(GameState.COMPLETED));

        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));

        gamePredictionRepository.save(new GamePrediction(twoWins, game1, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(twoWins, game2, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(oneWin, game1, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(oneWin, gameOutsideWeek, PredictionPick.HOME));

        predictionScoringService.scorePendingGames();

        // when
        List<WeeklyScoreParam> results = predictionScoringService.findWeeklyScores(monday, sunday);

        // then
        WeeklyScoreParam twoWinsResult = results.stream()
                .filter(r -> r.memberId().equals(twoWins.getId()))
                .findFirst().orElseThrow();
        WeeklyScoreParam oneWinResult = results.stream()
                .filter(r -> r.memberId().equals(oneWin.getId()))
                .findFirst().orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(twoWinsResult.score()).isEqualTo(2L);
            softAssertions.assertThat(oneWinResult.score()).isEqualTo(1L);
        });
    }
}
