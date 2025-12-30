package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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
        int savedCount = 0;

        for (GameCenterDetail detail : gameDetails) {
            try {
                boolean changed = saveToBronze(detail);
                if (changed) {
                    savedCount++;
                }
            } catch (Exception e) {
                log.error("[BRONZE] 경기 저장 실패: gameCode={}", detail.getGameCode(), e);
            }
        }

        log.info("[BRONZE] Processed {} games, {} saved (changed)", gameDetails.size(), savedCount);
        return savedCount;
    }

    /**
     * 개별 경기를 Bronze Layer에 저장
     */
    private boolean saveToBronze(GameCenterDetail detail) throws JsonProcessingException {
        // GameCenterDetail을 JSON으로 직렬화
        String payload = objectMapper.writeValueAsString(detail);

        // Natural Key 추출
        LocalDate date = parseDate(detail.getGameDate());
        String stadium = detail.getStadiumName();
        String homeTeam = detail.getHomeTeamName();
        String awayTeam = detail.getAwayTeamName();
        LocalTime startTime = parseTime(detail.getStartTime());

        // BronzeGameService를 활용하여 upsert
        boolean changed = bronzeGameService.upsertByNaturalKey(
                date, stadium, homeTeam, awayTeam, startTime, payload
        );

        if (changed) {
            log.debug("[BRONZE] Saved gameCode={}, {} vs {}",
                    detail.getGameCode(), awayTeam, homeTeam);
        }

        return changed;
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
