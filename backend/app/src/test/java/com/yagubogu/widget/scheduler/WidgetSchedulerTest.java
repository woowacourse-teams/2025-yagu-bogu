package com.yagubogu.widget.scheduler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.domain.TeamStatus;
import com.yagubogu.widget.domain.WidgetGamePush;
import com.yagubogu.widget.repository.WidgetGamePushRepository;
import com.yagubogu.widget.service.WidgetService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WidgetSchedulerTest {

    @Mock private GameRepository gameRepository;
    @Mock private WidgetGamePushRepository widgetGamePushRepository;
    @Mock private WidgetService widgetService;

    private WidgetScheduler widgetScheduler;

    /** 오늘 18:00 고정 Clock */
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 7, 21, 18, 0, 0);
    private static final LocalDate TODAY = NOW.toLocalDate();

    private final Clock fixedClock = Clock.fixed(
            NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        widgetScheduler = new WidgetScheduler(
                gameRepository, widgetGamePushRepository, widgetService, fixedClock);
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────────────────

    private Game mockGame(final long id, final Team home, final Team away) {
        final Game game = mock(Game.class);
        // lenient: END 전용 테스트에서 game 메서드가 호출되지 않을 수 있음
        lenient().when(game.getId()).thenReturn(id);
        lenient().when(game.getHomeTeam()).thenReturn(home);
        lenient().when(game.getAwayTeam()).thenReturn(away);
        return game;
    }

    private Team team(final String code) {
        return new Team("팀명", "약칭", code, TeamStatus.ACTIVE);
    }

    /**
     * 기본 모킹: 지연 START 검사에서 호출되는 findAll(), findScheduledGamesStartingBetween(deferred) 를
     * 빈 리스트로 막아 부작용 없게 합니다.
     */
    private void stubEmptyDeferredAndEnd() {
        // triggerDeferredStarts: MIDNIGHT ~ cutoff 범위 조회 → 빈 리스트
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT),
                any(LocalTime.class)
        )).thenReturn(List.of());

        // triggerEnds: findAll() → 빈 리스트
        when(widgetGamePushRepository.findAll()).thenReturn(List.of());
    }

    // ─── triggerScheduledStarts ───────────────────────────────────────────────

    @DisplayName("18:30 경기가 있을 때, 18:00 스케줄러 실행 시 START 푸시를 발송한다")
    @Test
    void run_sendsStartPush_forGameStartingIn30Min() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game game = mockGame(1L, ob, lg);

        // 정규 START 윈도우: now+29 ~ now+31 → 18:29 ~ 18:31
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of(game));

        when(gameRepository.existsLiveGameForTeam(TODAY, ob)).thenReturn(false);
        when(gameRepository.existsLiveGameForTeam(TODAY, lg)).thenReturn(false);
        stubEmptyDeferredAndEnd();

        // when
        widgetScheduler.run();

        // then
        verify(widgetService).sendStartPush(game);
    }

    @DisplayName("더블헤더: 홈팀이 LIVE 경기 중일 때 START를 발송하지 않는다")
    @Test
    void run_skipsStartPush_whenHomeTeamHasLiveGame() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game nextGame = mockGame(2L, ob, lg);

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of(nextGame));

        // 홈팀(OB)이 이미 LIVE 중
        when(gameRepository.existsLiveGameForTeam(TODAY, ob)).thenReturn(true);
        stubEmptyDeferredAndEnd();

        // when
        widgetScheduler.run();

        // then
        verify(widgetService, never()).sendStartPush(any());
    }

    @DisplayName("더블헤더: 원정팀이 LIVE 경기 중일 때도 START를 발송하지 않는다")
    @Test
    void run_skipsStartPush_whenAwayTeamHasLiveGame() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game nextGame = mockGame(2L, ob, lg);

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of(nextGame));

        when(gameRepository.existsLiveGameForTeam(TODAY, ob)).thenReturn(false);
        // 원정팀(LG)이 이미 LIVE 중
        when(gameRepository.existsLiveGameForTeam(TODAY, lg)).thenReturn(true);
        stubEmptyDeferredAndEnd();

        // when
        widgetScheduler.run();

        // then
        verify(widgetService, never()).sendStartPush(any());
    }

    @DisplayName("30분 전 윈도우에 경기가 없으면 START를 발송하지 않는다")
    @Test
    void run_doesNotSendStart_whenNoGameInWindow() {
        // given
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of());
        stubEmptyDeferredAndEnd();

        // when
        widgetScheduler.run();

        // then
        verify(widgetService, never()).sendStartPush(any());
    }

    // ─── triggerDeferredStarts ────────────────────────────────────────────────

    @DisplayName("더블헤더 지연 START: 앞 경기가 끝나면 대기 중인 경기에 START를 발송한다")
    @Test
    void run_sendsDeferredStart_whenPreviousGameEnded() {
        // given: 14:00 경기가 이미 끝난 상황, 18:30 경기는 아직 START 미발송
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game lateGame = mockGame(10L, ob, lg);

        // 정규 윈도우에는 없음
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of());

        // 지연 체크: MIDNIGHT ~ 18:29 범위 SCHEDULED 경기 = lateGame
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT), eq(LocalTime.of(17, 31))
        )).thenReturn(List.of(lateGame));

        // START 미발송 (findAll returns empty)
        when(widgetGamePushRepository.findAll()).thenReturn(List.of());

        // 팀 LIVE 경기 없음 (앞 경기 이미 종료)
        when(gameRepository.existsLiveGameForTeam(TODAY, ob)).thenReturn(false);
        when(gameRepository.existsLiveGameForTeam(TODAY, lg)).thenReturn(false);

        // when
        widgetScheduler.run();

        // then
        verify(widgetService).sendStartPush(lateGame);
    }

    @DisplayName("더블헤더 지연 START: 앞 경기가 아직 LIVE면 발송하지 않는다")
    @Test
    void run_doesNotSendDeferredStart_whenPreviousGameStillLive() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game lateGame = mockGame(10L, ob, lg);

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of());

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT), eq(LocalTime.of(17, 31))
        )).thenReturn(List.of(lateGame));

        when(widgetGamePushRepository.findAll()).thenReturn(List.of());

        // 홈팀(OB) 아직 LIVE 중
        when(gameRepository.existsLiveGameForTeam(TODAY, ob)).thenReturn(true);

        // when
        widgetScheduler.run();

        // then
        verify(widgetService, never()).sendStartPush(any());
    }

    @DisplayName("더블헤더 지연 START: 이미 START가 발송된 경기는 재발송하지 않는다")
    @Test
    void run_doesNotSendDeferredStart_whenAlreadySent() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game lateGame = mockGame(10L, ob, lg);

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of());

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT), eq(LocalTime.of(17, 31))
        )).thenReturn(List.of(lateGame));

        // gameId=10L에 대해 START가 이미 발송됨
        final WidgetGamePush alreadySent = new WidgetGamePush(10L);
        alreadySent.markStartSent(LocalDateTime.now());
        when(widgetGamePushRepository.findAll()).thenReturn(List.of(alreadySent));

        // when
        widgetScheduler.run();

        // then — START가 이미 발송됐으므로 pending 목록에서 제외됨
        verify(widgetService, never()).sendStartPush(any());
    }

    // ─── triggerEnds ──────────────────────────────────────────────────────────

    @DisplayName("COMPLETED 경기에 END 푸시를 발송한다")
    @Test
    void run_sendsEndPush_forCompletedGame() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game completedGame = mockGame(20L, ob, lg);
        when(completedGame.getGameState()).thenReturn(GameState.COMPLETED);

        final WidgetGamePush startSent = new WidgetGamePush(20L);
        startSent.markStartSent(LocalDateTime.now());

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of());
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT), eq(LocalTime.of(17, 31))
        )).thenReturn(List.of());

        when(widgetGamePushRepository.findAll()).thenReturn(List.of(startSent));
        when(gameRepository.findFinalizedGamesById(
                eq(TODAY),
                eq(List.of(GameState.COMPLETED, GameState.CANCELED)),
                eq(List.of(20L))
        )).thenReturn(List.of(completedGame));

        // when
        widgetScheduler.run();

        // then
        verify(widgetService).sendEndPush(completedGame);
    }

    @DisplayName("CANCELED 경기에도 END 푸시를 발송한다")
    @Test
    void run_sendsEndPush_forCanceledGame() {
        // given
        final Team ob = team("OB");
        final Team lg = team("LG");
        final Game canceledGame = mockGame(21L, ob, lg);
        when(canceledGame.getGameState()).thenReturn(GameState.CANCELED);

        final WidgetGamePush startSent = new WidgetGamePush(21L);
        startSent.markStartSent(LocalDateTime.now());

        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenReturn(List.of());
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT), eq(LocalTime.of(17, 31))
        )).thenReturn(List.of());

        when(widgetGamePushRepository.findAll()).thenReturn(List.of(startSent));
        when(gameRepository.findFinalizedGamesById(any(), any(), any()))
                .thenReturn(List.of(canceledGame));

        // when
        widgetScheduler.run();

        // then
        verify(widgetService).sendEndPush(canceledGame);
    }

    @DisplayName("START가 발송되지 않은 경기는 END 대상에 포함되지 않는다")
    @Test
    void run_doesNotSendEnd_whenStartNotSent() {
        // given: widgetGamePushRepository.findAll() = empty → END 대상 없음
        when(gameRepository.findScheduledGamesStartingBetween(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(widgetGamePushRepository.findAll()).thenReturn(List.of());

        // when
        widgetScheduler.run();

        // then
        verify(widgetService, never()).sendEndPush(any());
    }

    @DisplayName("이미 END가 발송된 경기는 재발송하지 않는다")
    @Test
    void run_doesNotSendEnd_whenAlreadySent() {
        // given
        final WidgetGamePush alreadyEnded = new WidgetGamePush(30L);
        alreadyEnded.markStartSent(LocalDateTime.now());
        alreadyEnded.markEndSent(LocalDateTime.now());

        when(gameRepository.findScheduledGamesStartingBetween(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(widgetGamePushRepository.findAll()).thenReturn(List.of(alreadyEnded));

        // when
        widgetScheduler.run();

        // then: endSent인 push는 필터링되어 findFinalizedGamesById 호출 안 됨
        verify(widgetService, never()).sendEndPush(any());
        verify(gameRepository, never()).findFinalizedGamesById(any(), any(), any());
    }

    @DisplayName("스케줄러 한 섹션이 예외를 던져도 나머지 섹션은 실행된다")
    @Test
    void run_continuesOtherSections_whenOneSectionThrows() {
        // given: triggerScheduledStarts에서 예외 발생
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.of(18, 29)), eq(LocalTime.of(18, 31))
        )).thenThrow(new RuntimeException("DB 연결 오류"));

        // 지연 START: 정상
        when(gameRepository.findScheduledGamesStartingBetween(
                eq(TODAY), eq(GameState.SCHEDULED),
                eq(LocalTime.MIDNIGHT), eq(LocalTime.of(17, 31))
        )).thenReturn(List.of());

        // END: 정상 (empty)
        when(widgetGamePushRepository.findAll()).thenReturn(List.of());

        // when: 예외가 전파되지 않아야 함
        widgetScheduler.run();

        // then: END 섹션은 실행됨 — triggerDeferredStarts + triggerEnds 총 2번 호출됨
        verify(widgetGamePushRepository, times(2)).findAll();
    }
}
