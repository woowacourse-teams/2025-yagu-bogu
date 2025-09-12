package com.yagubogu.game.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.game.service.client.KboGameSyncClient;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
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
class AdaptivePollerTest {

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
    private GameResultSyncService gameResultSyncService;

    private MutableClock clock;
    private AdaptivePoller poller;

    /** 테스트용 가변 Clock */
    static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        MutableClock(Instant start, ZoneId zone) {
            this.instant = start;
            this.zone = zone;
        }

        public void setInstant(final Instant instant) {
            this.instant = instant;
        }

        public void plusMinutes(long m) {
            instant = instant.plusSeconds(m * 60);
        }

        public void plusHours(long h) {
            instant = instant.plusSeconds(h * 3600);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    @BeforeEach
    void setUp() {
        clock = new MutableClock(
                LocalDate.of(2025, 7, 21).atTime(0, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                ZoneId.of("Asia/Seoul")
        );
        poller = new AdaptivePoller(gameRepository, kboGameSyncClient, gameResultSyncService, clock);
    }

    // ─────────────────────────────────────────────────────────────────────
    @DisplayName("초기화: FINALIZED 경기는 이후 폴링에서 호출되지 않는다")
    @Test
    void initialize_skipsFinalizedGame() {
        LocalDate today = LocalDate.of(2025, 7, 21);
        // COMPLETED(스킵 대상) + LIVE(대상)
        saveGame(today, "OB", "HT", "잠실구장", "FINAL", GameState.COMPLETED, LocalTime.of(18, 30));
        Game live = saveGame(today, "SS", "HH", "사직구장", "LIVE1", GameState.LIVE, LocalTime.of(18, 30));

        poller.initializeTodaySchedule(today);

        // due가 도달하도록 시간 전진 (킥오프 이후 + 여유)
        clock.plusHours(19);

        // fetch에 LIVE1만 내려오게 구성
        KboGameResponse row = new KboGameResponse("LIVE1", today, 0, LocalTime.of(18, 30),
                "잠실", "HH", "SS", GameState.LIVE);
        given(kboGameSyncClient.fetchGames(today))
                .willReturn(new KboGamesResponse(List.of(row), "100", "success"));

        // 실행
        poller.pollGameWhenReachDue();

        // then: FINAL은 호출되지 않고, LIVE1만 호출
        verify(gameResultSyncService, times(1)).updateGameDetails(eq("LIVE1"), any(KboGameResponse.class));
        verifyNoMoreInteractions(gameResultSyncService);
    }

    @DisplayName("전역 백오프: SCHEDULED(18:30) - 킥오프 전엔 대기, 이후 실패→백오프→회복한다")
    @Test
    void globalBackoff_withScheduledGameAt1830() {
        // 0) 고정 시각: 2025-07-21 18:00 (Asia/Seoul)
        LocalDate today = LocalDate.of(2025, 7, 21);
        clock.setInstant(today.atTime(18, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant());

        // 1) 경기 저장: SCHEDULED 18:30
        saveGame(today, "OB", "HT", "잠실구장", "G1", GameState.SCHEDULED, LocalTime.of(18, 30));
        poller.initializeTodaySchedule(today);

        // 2) 아직 킥오프 전(18:00 → 18:30)이라 첫 tick은 아무 일도 안 됨
        poller.pollGameWhenReachDue();
        verify(kboGameSyncClient, never()).fetchGames(eq(today));

        // 3) 시계를 18:31로 전진 → due(=18:30)를 넘김 → 첫 fetch 시도
        clock.plusMinutes(31); // 18:31
        given(kboGameSyncClient.fetchGames(eq(today)))
                .willThrow(new GameSyncException("boom1"))  // 첫 tick 실패 → 전역 백오프 시작
                .willReturn(new KboGamesResponse(
                        List.of(new KboGameResponse("G1", today, 0, LocalTime.of(18, 30), "잠실", "HT", "OB", GameState.SCHEDULED)),
                        "100", "success"));

        // 실패 tick → 전역 백오프 진입
        poller.pollGameWhenReachDue();
        verify(kboGameSyncClient, times(1)).fetchGames(eq(today));
        verify(gameResultSyncService, never()).updateGameDetails(anyString(), any());

        // 4) 백오프 창 내 재시도 → fetchGames 호출 자체가 없어야 함
        poller.pollGameWhenReachDue();
        verify(kboGameSyncClient, times(1)).fetchGames(eq(today));

        // 5) 백오프 창 경과 후 재시도(예: 2분 전진) → fetch + update 허용
        clock.plusMinutes(2);
        poller.pollGameWhenReachDue();
        verify(kboGameSyncClient, times(2)).fetchGames(eq(today));
        verify(gameResultSyncService, times(1)).updateGameDetails(eq("G1"), any(KboGameResponse.class));
    }

    @DisplayName("경기별 백오프: row 없음 → 백오프 창 전에는 호출 안 되고, 이후에는 호출한다")
    @Test
    void perGameBackoff_blocksPerGame_untilWindowPasses() {
        LocalDate today = LocalDate.of(2025, 7, 21);
        saveGame(today, "OB", "HT", "잠실구장", "G2", GameState.LIVE, LocalTime.of(18, 30));
        poller.initializeTodaySchedule(today);

        // due 도달
        clock.plusHours(19);

        // 첫 fetch: 다른 경기만 내려옴(=G2는 row 없음 → per-game backoff)
        KboGameResponse other = new KboGameResponse("OTHER", today, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.LIVE);
        given(kboGameSyncClient.fetchGames(today))
                .willReturn(new KboGamesResponse(List.of(other), "100", "success"));

        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, never()).updateGameDetails(eq("G2"), any());

        // 여전히 백오프 창: 곧바로 G2 row를 제공해도 호출되면 안 됨
        KboGameResponse row = new KboGameResponse("G2", today, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.LIVE);
        given(kboGameSyncClient.fetchGames(today))
                .willReturn(new KboGamesResponse(List.of(row), "100", "success"));

        // 백오프 1분 미만 가정 → 즉시 재시도해도 막혀야 함
        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, never()).updateGameDetails(eq("G2"), any());

        // 충분히 전진(>= 1분) → 호출 허용
        clock.plusMinutes(2);
        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, times(1)).updateGameDetails(eq("G2"), any(KboGameResponse.class));
    }

    @DisplayName("SCHEDULED 보호 주기: 킥오프 전/지연 상황에서도 최소 10~15분 주기가 유지된다(행동 검증)")
    @Test
    void scheduled_hasProtectionWindow_behaviorally() {
        LocalDate today = LocalDate.of(2025, 7, 21);
        String code = "S1";
        saveGame(today, "OB", "HT", "잠실구장", code, GameState.SCHEDULED, LocalTime.of(18, 30));

        poller.initializeTodaySchedule(today);

        // 킥오프 전(오전)에는 호출이 안 나와야 함: fetch에 row 제공해도 due 전엔 호출 X
        KboGameResponse row = new KboGameResponse(code, today, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.SCHEDULED);
        given(kboGameSyncClient.fetchGames(today))
                .willReturn(new KboGamesResponse(List.of(row), "100", "success"));

        // 오전 00:00 → 즉시 poll: due(18:30) 전이라 호출 X
        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, never()).updateGameDetails(eq(code), any());

        // 킥오프+10분 이후로 이동 → 호출 허용
        clock.plusHours(18);
        clock.plusMinutes(40); // 18:40
        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, times(1)).updateGameDetails(eq(code), any());
    }

    @DisplayName("LIVE 주기: due 이전엔 호출 없고, due 이후엔 1회 호출된다")
    @Test
    void live_interval_respected_behaviorally() {
        LocalDate today = LocalDate.of(2025, 7, 21);
        String code = "L1";
        saveGame(today, "OB", "HT", "잠실구장", code, GameState.LIVE, LocalTime.of(18, 30));

        poller.initializeTodaySchedule(today);

        // 킥오프 전에는 호출 X
        KboGameResponse row = new KboGameResponse(code, today, 0, LocalTime.of(18, 30),
                "잠실", "HT", "OB", GameState.LIVE);
        given(kboGameSyncClient.fetchGames(today))
                .willReturn(new KboGamesResponse(List.of(row), "100", "success"));

        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, never()).updateGameDetails(eq(code), any());

        // 킥오프 이후로 이동(18:31) → 호출 1회
        clock.plusHours(18);
        clock.plusMinutes(31);
        poller.pollGameWhenReachDue();
        verify(gameResultSyncService, times(1)).updateGameDetails(eq(code), any());
    }

    private Team team(String code) {
        return teamRepository.findByTeamCode(code).orElseThrow();
    }

    private Stadium stadium(String shortName) {
        return stadiumRepository.findByShortName(shortName).orElseThrow();
    }

    private Game saveGame(LocalDate date, String home, String away, String stadiumShort, String code, GameState state,
                          LocalTime startAt) {
        return gameFactory.save(b -> b
                .homeTeam(team(home))
                .awayTeam(team(away))
                .stadium(stadium(stadiumShort))
                .date(date)
                .startAt(startAt)
                .gameCode(code)
                .gameState(state)
        );
    }
}

