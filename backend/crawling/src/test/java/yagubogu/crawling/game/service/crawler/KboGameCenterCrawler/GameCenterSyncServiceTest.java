package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @DisplayName("saveToBronzeLayer - 현재 타자/투수를 games 테이블에 직접 반영한다")
    @Test
    void saveToBronzeLayer_UpdatesLiveBatterAndPitcher() {
        // given
        GameRepository gameRepository = mock(GameRepository.class);
        TeamRepository teamRepository = mock(TeamRepository.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        BronzeGameService bronzeGameService = mock(BronzeGameService.class);

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
        when(bronzeGameService.updateGameState(any(), any(), any(), any(), any(), any())).thenReturn(true);

        GameCenterSyncService service = new GameCenterSyncService(
                mock(KboGameCenterCrawler.class), bronzeGameService, gameRepository, teamRepository,
                stadiumRepository
        );

        GameCenterDetail detail = createDetail();

        // when
        service.saveToBronzeLayer(List.of(detail));

        // then
        verify(game).updateLiveBatterAndPitcher("away", "오명진", "home", "웰스");
    }

    @DisplayName("saveToBronzeLayer - 팀/구장을 찾을 수 없으면 타자/투수 갱신을 건너뛴다")
    @Test
    void saveToBronzeLayer_NoMatchingTeam_SkipsLiveStateUpdate() {
        // given
        GameRepository gameRepository = mock(GameRepository.class);
        TeamRepository teamRepository = mock(TeamRepository.class);
        StadiumRepository stadiumRepository = mock(StadiumRepository.class);
        BronzeGameService bronzeGameService = mock(BronzeGameService.class);

        when(teamRepository.findByShortName(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());
        when(stadiumRepository.findByLocation(org.mockito.ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        GameCenterSyncService service = new GameCenterSyncService(
                mock(KboGameCenterCrawler.class), bronzeGameService, gameRepository, teamRepository,
                stadiumRepository
        );

        // when & then (예외 없이 정상 종료되어야 함)
        service.saveToBronzeLayer(List.of(createDetail()));

        verify(gameRepository, never()).findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                any(), any(), any(), any(), any());
    }

    private GameCenterDetail createDetail() {
        GameCenterDetail detail = new GameCenterDetail();
        detail.setGameCode("20260621OBLG0");
        detail.setGameDate("20260621");
        detail.setStadiumName("잠실");
        detail.setHomeTeamName("LG");
        detail.setAwayTeamName("두산");
        detail.setStartTime("17:00");
        detail.setStatus("2회초");
        detail.setGameStatus(GameState.LIVE.name());
        detail.setCurrentBatterTeam("away");
        detail.setCurrentBatterName("오명진");
        detail.setCurrentPitcherTeam("home");
        detail.setCurrentPitcherName("웰스");

        return detail;
    }
}
