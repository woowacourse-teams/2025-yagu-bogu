package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.service.BronzeGameService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yagubogu.crawling.game.dto.GameCenterDetail;

class GameCenterSyncServiceTest {

    private final KboGameCenterCrawler crawler = mock(KboGameCenterCrawler.class);
    private final BronzeGameService bronzeGameService = mock(BronzeGameService.class);
    private final GameCenterSyncService service = new GameCenterSyncService(crawler, bronzeGameService);

    @DisplayName("GameCenter의 경기예정 상태를 SCHEDULED로 저장한다")
    @Test
    void saveScheduledGame() {
        GameCenterDetail detail = gameDetail("경기예정");
        when(bronzeGameService.updateGameState(
                "20260814HHSS0",
                LocalDate.of(2026, 8, 14),
                "대구",
                "삼성",
                "한화",
                LocalTime.of(19, 0),
                GameState.SCHEDULED
        )).thenReturn(true);

        int updatedCount = service.saveToBronzeLayer(List.of(detail));

        assertThat(updatedCount).isEqualTo(1);
        verify(bronzeGameService).updateGameState(
                "20260814HHSS0",
                LocalDate.of(2026, 8, 14),
                "대구",
                "삼성",
                "한화",
                LocalTime.of(19, 0),
                GameState.SCHEDULED
        );
    }

    @DisplayName("알 수 없는 GameCenter 상태는 기존 Bronze 상태를 덮어쓰지 않는다")
    @Test
    void skipUnknownGameState() {
        GameCenterDetail detail = gameDetail("중계 준비");

        int updatedCount = service.saveToBronzeLayer(List.of(detail));

        assertThat(updatedCount).isZero();
        verifyNoInteractions(bronzeGameService);
    }

    private GameCenterDetail gameDetail(final String status) {
        GameCenterDetail detail = new GameCenterDetail();
        detail.setGameCode("20260814HHSS0");
        detail.setGameDate("20260814");
        detail.setStadiumName("대구");
        detail.setHomeTeamName("삼성");
        detail.setAwayTeamName("한화");
        detail.setStartTime("19:00");
        detail.setStatus(status);
        return detail;
    }
}
