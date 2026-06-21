package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.game.service.GameEtlService;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.support.TransactionTemplate;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.repository.GameJdbcBatchUpsertRepository;

class KboScoreboardServiceTest {

    @DisplayName("updateFromScoreboard - 진루정보/카운트를 게임 라이브 상태로 반영한다")
    @Test
    void updateFromScoreboard_UpdatesLiveBaseState() {
        // given
        String gameCode = "20260621SKNC0";
        GameRepository gameRepository = mock(GameRepository.class);
        Game game = mock(Game.class);
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(game));

        KboScoreboardService service = new KboScoreboardService(
                mock(KboScoreboardCrawler.class),
                mock(KboScoreboardMapper.class),
                mock(GameJdbcBatchUpsertRepository.class),
                mock(TeamRepository.class),
                mock(StadiumRepository.class),
                mock(TransactionTemplate.class),
                mock(TransactionTemplate.class),
                gameRepository,
                mock(BronzeGameService.class),
                mock(GameEtlService.class),
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
        verify(game).updateLiveBaseState(true, true, false, 1, 2, 0);
    }

    @DisplayName("updateFromScoreboard - gameCode에 해당하는 Game이 없으면 라이브 상태 갱신을 건너뛴다")
    @Test
    void updateFromScoreboard_NoMatchingGame_SkipsLiveStateUpdate() {
        // given
        String gameCode = "20260621SKNC0";
        GameRepository gameRepository = mock(GameRepository.class);
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.empty());

        KboScoreboardService service = new KboScoreboardService(
                mock(KboScoreboardCrawler.class),
                mock(KboScoreboardMapper.class),
                mock(GameJdbcBatchUpsertRepository.class),
                mock(TeamRepository.class),
                mock(StadiumRepository.class),
                mock(TransactionTemplate.class),
                mock(TransactionTemplate.class),
                gameRepository,
                mock(BronzeGameService.class),
                mock(GameEtlService.class),
                createObjectMapper(),
                mock(ApplicationEventPublisher.class)
        );

        // when & then (예외 없이 정상 종료되어야 함)
        service.updateFromScoreboard(gameCode, createScoreboardGame());
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
