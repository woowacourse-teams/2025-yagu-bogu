package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.service.BronzeGameService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCenterSyncService {

    private final KboGameCenterCrawler crawler;
    private final BronzeGameService bronzeGameService;

    /**
     * 특정 날짜 경기 상세 정보 수집 및 Bronze Layer 저장
     */
    public int fetchGameCenter(LocalDate date) {
        GameCenter dailyData = fetchGameCenterOnly(date);

        return saveToBronzeLayer(dailyData.getGames());
    }

    public GameCenter fetchGameCenterOnly(LocalDate date) {
        return crawler.fetchDailyGameCenter(date);
    }

    /**
     * GameCenterDetail 리스트를 받아서 Bronze Layer에 저장
     */
    public int saveToBronzeLayer(java.util.List<GameCenterDetail> gameDetails) {
        int updatedCount = 0;

        for (GameCenterDetail detail : gameDetails) {
            try {
                boolean updated = updateGameState(detail);
                if (updated) {
                    updatedCount++;
                }
            } catch (Exception e) {
                log.error("[BRONZE] 경기 상태 저장 실패: gameCode={}", detail.getGameCode(), e);
            }
        }

        log.info("[BRONZE] Processed {} games, {} state updates", gameDetails.size(), updatedCount);
        return updatedCount;
    }

    /**
     * 개별 경기 상태를 Bronze Layer에 반영
     */
    private boolean updateGameState(GameCenterDetail detail) {
        LocalDate date = parseDate(detail.getGameDate());
        String stadium = detail.getStadiumName();
        String homeTeam = detail.getHomeTeamName();
        String awayTeam = detail.getAwayTeamName();
        LocalTime startTime = parseTime(detail.getStartTime());
        GameState state = GameState.fromName(detail.getStatus());

        boolean updated = bronzeGameService.updateGameState(
                date, stadium, homeTeam, awayTeam, startTime, state
        );

        if (updated) {
            log.debug("[BRONZE] Game state synced: gameCode={}, state={}",
                    detail.getGameCode(), state);
        }

        return updated;
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
