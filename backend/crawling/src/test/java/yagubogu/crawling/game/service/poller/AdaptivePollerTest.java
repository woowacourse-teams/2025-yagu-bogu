package yagubogu.crawling.game.service.poller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.domain.TeamStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.service.GameReadOnlyService;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

class AdaptivePollerTest {

    @DisplayName("시작 시각이 변경되어도 KBO gameCode로 기존 경기를 찾아 갱신한다")
    @Test
    void pollByGameCodeWhenStartTimeChanged() {
        GameReadOnlyService gameReadOnlyService = mock(GameReadOnlyService.class);
        KboScoreboardService scoreboardService = mock(KboScoreboardService.class);
        GameCenterSyncService gameCenterSyncService = mock(GameCenterSyncService.class);
        GameScheduleManager scheduleManager = mock(GameScheduleManager.class);
        BackoffStrategy backoffStrategy = mock(BackoffStrategy.class);
        GlobalBackOffManager globalBackoff = mock(GlobalBackOffManager.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-11T10:30:00Z"), ZoneId.of("Asia/Seoul"));
        AdaptivePoller poller = new AdaptivePoller(
                gameReadOnlyService,
                scoreboardService,
                gameCenterSyncService,
                scheduleManager,
                backoffStrategy,
                globalBackoff,
                clock
        );

        LocalDate date = LocalDate.of(2026, 8, 11);
        String gameCode = "20260811KTNC0";
        Team homeTeam = new Team("NC 다이노스", "NC", "NC", TeamStatus.ACTIVE);
        Team awayTeam = new Team("KT 위즈", "KT", "KT", TeamStatus.ACTIVE);
        Stadium stadium = new Stadium("창원NC파크", "창원", "창원", 0.0, 0.0, StadiumLevel.MAIN);
        Game game = new Game(
                stadium, homeTeam, awayTeam, date, LocalTime.of(18, 30), gameCode,
                null, null, null, null, null, null, GameState.SCHEDULED
        );
        ReflectionTestUtils.setField(game, "id", 1L);
        KboScoreboardGame fetched = new KboScoreboardGame(
                gameCode,
                date,
                "경기중",
                "창원",
                LocalTime.of(19, 0),
                null,
                new KboScoreboardTeam("KT", 0, 1, 0, 0, List.of()),
                new KboScoreboardTeam("NC", 1, 2, 0, 1, List.of()),
                0,
                1,
                null,
                null,
                null
        );

        when(globalBackoff.isActive(any())).thenReturn(false);
        when(scheduleManager.shouldWake(any())).thenReturn(true);
        when(gameReadOnlyService.existsByDateAndGameStateIn(any(), any())).thenReturn(true);
        when(scoreboardService.fetchScoreboardOnly(date)).thenReturn(List.of(fetched));
        when(gameReadOnlyService.findAllByDateWithStadium(date)).thenReturn(List.of(game));
        when(scheduleManager.shouldPollGame(1L, Instant.now(clock))).thenReturn(true);

        poller.pollGameWhenReachDue();

        verify(gameCenterSyncService).fetchGameCenter(date);
        verify(scoreboardService).updateFromScoreboard(gameCode, fetched);
        verify(scheduleManager).scheduleNextPoll(1L);
    }

    @DisplayName("게임센터 갱신 실패가 스코어보드 수집을 막지 않는다")
    @Test
    void continueScoreboardPollingWhenGameCenterRefreshFails() {
        GameReadOnlyService gameReadOnlyService = mock(GameReadOnlyService.class);
        KboScoreboardService scoreboardService = mock(KboScoreboardService.class);
        GameCenterSyncService gameCenterSyncService = mock(GameCenterSyncService.class);
        GameScheduleManager scheduleManager = mock(GameScheduleManager.class);
        BackoffStrategy backoffStrategy = mock(BackoffStrategy.class);
        GlobalBackOffManager globalBackoff = mock(GlobalBackOffManager.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-11T10:30:00Z"), ZoneId.of("Asia/Seoul"));
        AdaptivePoller poller = new AdaptivePoller(
                gameReadOnlyService,
                scoreboardService,
                gameCenterSyncService,
                scheduleManager,
                backoffStrategy,
                globalBackoff,
                clock
        );
        LocalDate date = LocalDate.of(2026, 8, 11);

        when(globalBackoff.isActive(any())).thenReturn(false);
        when(scheduleManager.shouldWake(any())).thenReturn(true);
        when(gameReadOnlyService.existsByDateAndGameStateIn(any(), any())).thenReturn(true);
        when(gameCenterSyncService.fetchGameCenter(date)).thenThrow(new RuntimeException("game center unavailable"));
        when(scoreboardService.fetchScoreboardOnly(date)).thenReturn(List.of());

        poller.pollGameWhenReachDue();

        verify(gameCenterSyncService).fetchGameCenter(date);
        verify(scoreboardService).fetchScoreboardOnly(date);
    }

    @DisplayName("상태가 경기전으로 같아도 시작 시각이 바뀌면 갱신한다")
    @Test
    void updateScheduledGameWhenStartTimeChanged() {
        GameReadOnlyService gameReadOnlyService = mock(GameReadOnlyService.class);
        KboScoreboardService scoreboardService = mock(KboScoreboardService.class);
        GameCenterSyncService gameCenterSyncService = mock(GameCenterSyncService.class);
        GameScheduleManager scheduleManager = mock(GameScheduleManager.class);
        BackoffStrategy backoffStrategy = mock(BackoffStrategy.class);
        GlobalBackOffManager globalBackoff = mock(GlobalBackOffManager.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-13T09:30:00Z"), ZoneId.of("Asia/Seoul"));
        AdaptivePoller poller = new AdaptivePoller(
                gameReadOnlyService, scoreboardService, gameCenterSyncService, scheduleManager,
                backoffStrategy, globalBackoff, clock
        );

        LocalDate date = LocalDate.of(2026, 8, 13);
        String gameCode = "20260813HHOB0";
        Team homeTeam = new Team("두산 베어스", "두산", "OB", TeamStatus.ACTIVE);
        Team awayTeam = new Team("한화 이글스", "한화", "HH", TeamStatus.ACTIVE);
        Stadium stadium = new Stadium("서울종합운동장 야구장", "잠실", "잠실", 0.0, 0.0, StadiumLevel.MAIN);
        Game game = new Game(
                stadium, homeTeam, awayTeam, date, LocalTime.of(18, 30), gameCode,
                null, null, null, null, null, null, GameState.SCHEDULED
        );
        ReflectionTestUtils.setField(game, "id", 1L);
        KboScoreboardGame fetched = new KboScoreboardGame(
                gameCode,
                date,
                "경기전",
                "잠실",
                LocalTime.of(19, 0),
                null,
                new KboScoreboardTeam("한화", null, null, null, null, List.of()),
                new KboScoreboardTeam("두산", null, null, null, null, List.of()),
                null,
                null,
                null,
                null,
                null
        );

        when(globalBackoff.isActive(any())).thenReturn(false);
        when(scheduleManager.shouldWake(any())).thenReturn(true);
        when(gameReadOnlyService.existsByDateAndGameStateIn(any(), any())).thenReturn(true);
        when(scoreboardService.fetchScoreboardOnly(date)).thenReturn(List.of(fetched));
        when(gameReadOnlyService.findAllByDateWithStadium(date)).thenReturn(List.of(game));
        when(scheduleManager.shouldPollGame(1L, Instant.now(clock))).thenReturn(true);

        poller.pollGameWhenReachDue();

        verify(scoreboardService).updateFromScoreboard(gameCode, fetched);
        verify(scheduleManager).scheduleNextPoll(1L);
    }
}
