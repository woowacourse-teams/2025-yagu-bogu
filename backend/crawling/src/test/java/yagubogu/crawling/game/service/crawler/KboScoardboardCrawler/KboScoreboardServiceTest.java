package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.game.service.GameEtlService;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.StadiumLevel;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.domain.TeamStatus;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;
import yagubogu.crawling.game.dto.GameDateCrawlResponse;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.repository.GameJdbcBatchUpsertRepository;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;

class KboScoreboardServiceTest {

    @DisplayName("updateFromScoreboard - 진루정보/카운트를 GameCenterSyncService에 위임해 게임 라이브 상태로 반영한다")
    @Test
    void updateFromScoreboard_DelegatesLiveBaseStateToGameCenterSyncService() {
        // given
        String gameCode = "20260621SKNC0";
        GameCenterSyncService gameCenterSyncService = mock(GameCenterSyncService.class);

        KboScoreboardService service = new KboScoreboardService(
                mock(KboScoreboardCrawler.class),
                mock(KboScoreboardMapper.class),
                mock(GameJdbcBatchUpsertRepository.class),
                mock(TeamRepository.class),
                mock(StadiumRepository.class),
                mock(TransactionTemplate.class),
                mock(TransactionTemplate.class),
                mock(BronzeGameService.class),
                mock(GameEtlService.class),
                gameCenterSyncService,
                createObjectMapper(),
                mock(ApplicationEventPublisher.class)
        );

        KboScoreboardGame data = createScoreboardGame();
        data.setFirstBaseOccupied(true);
        data.setSecondBaseOccupied(true);
        data.setThirdBaseOccupied(false);
        data.setBalls(1);
        data.setStrikes(2);
        data.setOuts(0);

        // when
        service.updateFromScoreboard(gameCode, data);

        // then
        verify(gameCenterSyncService).updateLiveBaseState(gameCode, true, true, false, 1, 2, 0);
    }

    @DisplayName("예정 경기의 gameCode는 GameCenter의 공식 g_id를 사용한다")
    @Test
    void fetchOfficialGameCodeFromGameCenterForScheduledGame() {
        LocalDate date = LocalDate.of(2026, 8, 13);
        String gameCode = "20260813HHOB0";
        KboScoreboardGame scheduledGame = new KboScoreboardGame(
                date,
                "경기전",
                "잠실",
                LocalTime.of(18, 30),
                null,
                new KboScoreboardTeam("한화", null, null, null, null, List.of()),
                new KboScoreboardTeam("두산", null, null, null, null, List.of()),
                null,
                null,
                null,
                null,
                null
        );

        GameCenterDetail detail = new GameCenterDetail();
        detail.setGameCode(gameCode);
        detail.setGameSc("1");
        detail.setStatus("경기전");
        GameCenter gameCenter = new GameCenter();
        gameCenter.setGames(List.of(detail));

        KboScoreboardCrawler crawler = mock(KboScoreboardCrawler.class);
        TeamRepository teamRepository = mock(TeamRepository.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        TransactionTemplate transactionTemplate = executeCallbacksImmediately();
        TransactionTemplate readOnlyTransactionTemplate = executeCallbacksImmediately();
        BronzeGameService bronzeGameService = mock(BronzeGameService.class);
        GameEtlService gameEtlService = mock(GameEtlService.class);
        GameCenterSyncService gameCenterSyncService = mock(GameCenterSyncService.class);

        Team awayTeam = new Team("한화 이글스", "한화", "HH", TeamStatus.ACTIVE);
        Team homeTeam = new Team("두산 베어스", "두산", "OB", TeamStatus.ACTIVE);
        Stadium stadium = new Stadium("서울종합운동장 야구장", "잠실", "잠실", 0.0, 0.0, StadiumLevel.MAIN);

        when(crawler.crawl(List.of(date))).thenReturn(Map.of(date, List.of(scheduledGame)));
        when(teamRepository.findAll()).thenReturn(List.of(awayTeam, homeTeam));
        when(stadiumRepository.findAll()).thenReturn(List.of(stadium));
        when(bronzeGameService.upsertByNaturalKey(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(true);
        when(gameCenterSyncService.fetchGameCenterOnly(date)).thenReturn(gameCenter);
        when(gameEtlService.reprocessDate(date)).thenReturn(1);

        KboScoreboardService service = new KboScoreboardService(
                crawler,
                mock(KboScoreboardMapper.class),
                mock(GameJdbcBatchUpsertRepository.class),
                teamRepository,
                stadiumRepository,
                transactionTemplate,
                readOnlyTransactionTemplate,
                bronzeGameService,
                gameEtlService,
                gameCenterSyncService,
                new ObjectMapper().findAndRegisterModules(),
                mock(ApplicationEventPublisher.class)
        );

        GameDateCrawlResponse response = service.fetchGamesByDate(date);

        verify(gameCenterSyncService).saveToBronzeLayer(List.of(detail));
        verify(gameEtlService).reprocessDate(date);
        assertThat(response.requested()).isEqualTo(1);
        assertThat(response.matched()).isEqualTo(1);
        assertThat(response.transformed()).isEqualTo(1);
        assertThat(response.savedGameCodes()).containsExactly(gameCode);
        assertThat(response.completedGameCodes()).isEmpty();
    }

    private TransactionTemplate executeCallbacksImmediately() {
        TransactionTemplate template = mock(TransactionTemplate.class);
        when(template.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        return template;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private KboScoreboardGame createScoreboardGame() {
        KboScoreboardTeam awayTeam = new KboScoreboardTeam("SSG", 0, 0, 0, 0, List.of());
        KboScoreboardTeam homeTeam = new KboScoreboardTeam("NC", 0, 0, 0, 0, List.of());

        return new KboScoreboardGame(
                LocalDate.of(2026, 6, 21),
                "4회초",
                "창원",
                LocalTime.of(17, 0),
                null,
                awayTeam,
                homeTeam,
                1,
                0,
                null,
                null,
                null
        );
    }
}
