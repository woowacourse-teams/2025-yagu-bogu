package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yagubogu.crawling.game.dto.GameCenterDetail;

class GameCenterSyncServiceTest {

    private final KboGameCenterCrawler crawler = mock(KboGameCenterCrawler.class);
    private final BronzeGameService bronzeGameService = mock(BronzeGameService.class);
    private final GameRepository gameRepository = mock(GameRepository.class);
    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final StadiumRepository stadiumRepository = mock(StadiumRepository.class);
    private final GameCenterSyncService service = new GameCenterSyncService(
            crawler, bronzeGameService, gameRepository, teamRepository, stadiumRepository
    );

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

    @DisplayName("saveToBronzeLayer - 선발 예고 투수를 games 테이블에 직접 반영한다")
    @Test
    void saveToBronzeLayer_UpdatesProbablePitchers() {
        // given
        Team homeTeam = mock(Team.class);
        Team awayTeam = mock(Team.class);
        Stadium stadium = mock(Stadium.class);
        Game game = mock(Game.class);

        when(teamRepository.findByShortName("삼성")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findByShortName("한화")).thenReturn(Optional.of(awayTeam));
        when(stadiumRepository.findByLocation("대구")).thenReturn(Optional.of(stadium));
        when(gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                LocalDate.of(2026, 8, 14), stadium, homeTeam, awayTeam, LocalTime.of(19, 0)
        )).thenReturn(Optional.of(game));

        GameCenterDetail detail = gameDetail("경기예정");
        detail.setHomeProbablePitcher("페덱");
        detail.setAwayProbablePitcher("로건");

        // when
        service.saveToBronzeLayer(List.of(detail));

        // then
        verify(game).updateProbablePitchers("페덱", "로건");
    }

    @DisplayName("saveToBronzeLayer - 선발 예고 투수가 없으면 갱신을 건너뛴다")
    @Test
    void saveToBronzeLayer_NoProbablePitcher_SkipsUpdate() {
        // given
        GameCenterDetail detail = gameDetail("경기중");

        // when & then (예외 없이 정상 종료되어야 함)
        service.saveToBronzeLayer(List.of(detail));

        verify(gameRepository, never()).findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                any(), any(), any(), any(), any());
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
