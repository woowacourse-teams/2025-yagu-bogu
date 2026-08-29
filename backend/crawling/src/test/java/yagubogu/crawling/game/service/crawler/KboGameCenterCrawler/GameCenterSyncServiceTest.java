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

    @DisplayName("saveToBronzeLayer - 현재 타자/투수를 games 테이블에 직접 반영한다")
    @Test
    void saveToBronzeLayer_UpdatesLiveBatterAndPitcher() {
        // given
        Team homeTeam = mock(Team.class);
        Team awayTeam = mock(Team.class);
        Stadium stadium = mock(Stadium.class);
        Game game = mock(Game.class);

        when(teamRepository.findByShortName("LG")).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findByShortName("두산")).thenReturn(Optional.of(awayTeam));
        when(stadiumRepository.findByLocation("잠실")).thenReturn(Optional.of(stadium));
        when(gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                LocalDate.of(2026, 6, 21), stadium, homeTeam, awayTeam, LocalTime.of(17, 0)
        )).thenReturn(Optional.of(game));

        GameCenterDetail detail = new GameCenterDetail();
        detail.setGameCode("20260621OBLG0");
        detail.setGameDate("20260621");
        detail.setStadiumName("잠실");
        detail.setHomeTeamName("LG");
        detail.setAwayTeamName("두산");
        detail.setStartTime("17:00");
        detail.setStatus("2회초");
        detail.setCurrentBatterTeam("away");
        detail.setCurrentBatterName("오명진");
        detail.setCurrentPitcherTeam("home");
        detail.setCurrentPitcherName("웰스");

        // when
        service.saveToBronzeLayer(List.of(detail));

        // then
        verify(game).updateLiveBatterAndPitcher("away", "오명진", "home", "웰스");
    }

    @DisplayName("saveToBronzeLayer - 팀/구장을 찾을 수 없으면 타자/투수 갱신을 건너뛴다")
    @Test
    void saveToBronzeLayer_NoMatchingTeam_SkipsLiveStateUpdate() {
        // given
        when(teamRepository.findByShortName(any())).thenReturn(Optional.empty());
        when(stadiumRepository.findByLocation(any())).thenReturn(Optional.empty());

        GameCenterDetail detail = gameDetail("2회초");
        detail.setCurrentBatterName("오명진");

        // when & then (예외 없이 정상 종료되어야 함)
        service.saveToBronzeLayer(List.of(detail));

        verify(gameRepository, never()).findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                any(), any(), any(), any(), any());
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

    @DisplayName("updateLiveBaseState - gameCode에 해당하는 Game의 진루정보/카운트를 갱신한다")
    @Test
    void updateLiveBaseState_UpdatesGame() {
        // given
        String gameCode = "20260621SKNC0";
        Game game = mock(Game.class);
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.of(game));

        // when
        service.updateLiveBaseState(gameCode, true, true, false, 1, 2, 0);

        // then
        verify(game).updateLiveBaseState(true, true, false, 1, 2, 0);
    }

    @DisplayName("updateLiveBaseState - gameCode에 해당하는 Game이 없으면 갱신을 건너뛴다")
    @Test
    void updateLiveBaseState_NoMatchingGame_Skips() {
        // given
        String gameCode = "20260621SKNC0";
        when(gameRepository.findByGameCode(gameCode)).thenReturn(Optional.empty());

        // when & then (예외 없이 정상 종료되어야 함)
        service.updateLiveBaseState(gameCode, true, true, false, 1, 2, 0);
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
