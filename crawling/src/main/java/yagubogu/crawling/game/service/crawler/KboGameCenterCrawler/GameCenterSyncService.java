package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yagubogu.crawling.game.dto.GameCenterDetail;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCenterSyncService {

    private final KboGameCenterCrawler crawler;
    private final GameRepository gameRepository;
    private final StadiumRepository stadiumRepository;
    private final TeamRepository teamRepository;

    /**
     * 특정 날짜 경기 상세 정보 수집
     */
    public GameCenter fetchGameCenter(LocalDate date) {
        GameCenter dailyData = crawler.fetchDailyGameCenter(date);
        updateGameStates(dailyData.getGames());
        return dailyData;
    }

    /**
     * GameDetailInfo 리스트를 받아서 Game 상태 업데이트
     */
    private void updateGameStates(List<GameCenterDetail> gameDetails) {
        for (GameCenterDetail detail : gameDetails) {
            try {
                updateOrCreateGame(detail);
            } catch (Exception e) {
                log.error("경기 업데이트 실패: gameCode={}", detail.getGameId(), e);
            }
        }
    }

    /**
     * 개별 경기 업데이트 또는 생성
     */
    private void updateOrCreateGame(GameCenterDetail detail) {
        // GameState 변환
        GameState gameState = fromGameSc(detail.getGameSc());

        // gameCode로 조회 또는 생성
        Game game = gameRepository.findByGameCode(detail.getGameId())
                .orElseGet(() -> createNewGame(detail));

        // 상태 업데이트
        if (game.getGameState() != gameState) {
            game.updateGameState(gameState);
            log.info("경기 상태 업데이트: gameCode={}, {} vs {} - {} → {}",
                    detail.getGameId(),
                    detail.getAwayTeamName(),
                    detail.getHomeTeamName(),
                    game.getGameState(),
                    gameState);
        }
    }

    private GameState fromGameSc(final String gameSc) {
        if (gameSc == null || gameSc.isBlank()) {
            return GameState.SCHEDULED;  // 기본값
        }

        try {
            Integer scNumber = Integer.parseInt(gameSc);
            return GameState.from(scNumber);
        } catch (NumberFormatException e) {
            throw new GameSyncException("Invalid gameSc format: " + gameSc);
        }
    }

    /**
     * 새 경기 생성
     */
    private Game createNewGame(GameCenterDetail detail) {
        Stadium stadium = findStadium(detail.getStadiumName());
        Team homeTeam = findTeam(detail.getHomeTeamCode());
        Team awayTeam = findTeam(detail.getAwayTeamCode());

        LocalDate date = parseDate(detail.getGameDate());
        LocalTime startAt = parseTime(detail.getStartTime());
        GameState gameState = fromGameSc(detail.getGameSc());

        Game newGame = new Game(
                stadium,
                homeTeam,
                awayTeam,
                date,
                startAt,
                detail.getGameId(),  // gameCode
                null,  // homeScore
                null,  // awayScore
                null,  // homeScoreBoard
                null,  // awayScoreBoard
                null,  // homePitcher
                null,  // awayPitcher
                gameState
        );

        log.info("새 경기 생성: gameCode={}, {} vs {}",
                detail.getGameId(),
                detail.getAwayTeamName(),
                detail.getHomeTeamName());

        return gameRepository.save(newGame);
    }

    /**
     * Stadium 조회
     */
    private Stadium findStadium(String stadiumName) {
        return stadiumRepository.findByLocation(stadiumName)
                .orElseThrow(() -> new GameSyncException("Stadium not found: " + stadiumName));
    }

    /**
     * Team 조회
     */
    private Team findTeam(String teamId) {
        return teamRepository.findByTeamCode(teamId)
                .orElseThrow(() -> new GameSyncException("Team not found: " + teamId));
    }

    /**
     * 날짜 파싱: "20251021" → LocalDate
     */
    private LocalDate parseDate(String gameDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            return LocalDate.parse(gameDate, formatter);
        } catch (Exception e) {
            throw new GameSyncException("Invalid date format: " + gameDate);
        }
    }

    /**
     * 시간 파싱: "18:30" → LocalTime
     */
    private LocalTime parseTime(String startTime) {
        try {
            return LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new GameSyncException("Invalid time format: " + startTime);
        }
    }
}
