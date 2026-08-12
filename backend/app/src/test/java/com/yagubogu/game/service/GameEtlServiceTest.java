package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.domain.BronzeGame;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.BronzeGameRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.domain.TeamStatus;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

class GameEtlServiceTest {

    private final BronzeGameRepository bronzeGameRepository = mock(BronzeGameRepository.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final StadiumRepository stadiumRepository = mock(StadiumRepository.class);
    private final TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);

    private GameEtlService gameEtlService;

    @BeforeEach
    void setUp() {
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });

        Clock clock = Clock.fixed(Instant.parse("2026-08-12T00:00:00Z"), ZoneId.of("Asia/Seoul"));
        gameEtlService = new GameEtlService(
                bronzeGameRepository,
                gameRepository,
                teamRepository,
                stadiumRepository,
                new ObjectMapper().findAndRegisterModules(),
                clock,
                transactionTemplate
        );
    }

    @DisplayName("공식 gameCode와 시작 시각이 달라져도 같은 대진의 기존 경기를 갱신한다")
    @Test
    void transformByGameCodeWhenStartTimeChanged() {
        LocalDate date = LocalDate.of(2026, 8, 11);
        String gameCode = "20260811KTNC0";
        String legacyGameCode = "20260811KTNC1";
        Team homeTeam = new Team("NC 다이노스", "NC", "NC", TeamStatus.ACTIVE);
        Team awayTeam = new Team("KT 위즈", "KT", "KT", TeamStatus.ACTIVE);
        Stadium stadium = new Stadium("창원NC파크", "창원", "창원", 0.0, 0.0, StadiumLevel.MAIN);
        Game existingGame = new Game(
                stadium, homeTeam, awayTeam, date, LocalTime.of(18, 30), legacyGameCode,
                null, null, null, null, null, null, GameState.SCHEDULED
        );
        String payload = """
                {
                  "gameCode":"20260811KTNC0",
                  "date":"2026-08-11",
                  "status":"경기종료",
                  "stadium":"창원",
                  "startTime":"19:00:00",
                  "awayScore":0,
                  "homeScore":3,
                  "winningPitcher":"구창모",
                  "losingPitcher":"소형준",
                  "awayTeamScoreboard":{"name":"KT","runs":0,"hits":4,"errors":0,"basesOnBalls":1,"inningScores":[]},
                  "homeTeamScoreboard":{"name":"NC","runs":3,"hits":8,"errors":0,"basesOnBalls":2,"inningScores":[]}
                }
                """;
        BronzeGame bronzeGame = new BronzeGame(
                gameCode, date, "창원", "NC", "KT", LocalTime.of(19, 0),
                LocalDateTime.of(2026, 8, 12, 0, 0), payload, "hash"
        );
        ReflectionTestUtils.setField(bronzeGame, "id", 1L);

        when(bronzeGameRepository.findPendingEtlByDateRange(date, date)).thenReturn(java.util.List.of(bronzeGame));
        when(bronzeGameRepository.findById(1L)).thenReturn(Optional.of(bronzeGame));
        when(teamRepository.findByShortName("NC")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findByShortName("KT")).thenReturn(Optional.of(awayTeam));
        when(stadiumRepository.findByLocation("창원")).thenReturn(Optional.of(stadium));
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.empty());
        when(gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                date, stadium, homeTeam, awayTeam, LocalTime.of(19, 0)
        )).thenReturn(Optional.empty());
        when(gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeam(
                date, stadium, homeTeam, awayTeam
        )).thenReturn(java.util.List.of(existingGame));

        int transformed = gameEtlService.transformPendingDateRange(date, date);

        assertThat(transformed).isEqualTo(1);
        assertThat(existingGame.getStartAt()).isEqualTo(LocalTime.of(19, 0));
        assertThat(existingGame.getGameCode()).isEqualTo(gameCode);
        assertThat(existingGame.getHomeScore()).isEqualTo(3);
        assertThat(existingGame.getAwayScore()).isZero();
        assertThat(existingGame.getGameState()).isEqualTo(GameState.COMPLETED);
        assertThat(bronzeGame.getEtlProcessedAt()).isNotNull();
    }
}
