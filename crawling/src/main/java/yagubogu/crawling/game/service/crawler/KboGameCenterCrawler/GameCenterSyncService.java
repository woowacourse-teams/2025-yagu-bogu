package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yagubogu.crawling.game.dto.GameInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCenterSyncService {

    private final KboGameCenterCrawler crawler;
    private final KboGameCenterMapper mapper;
    private final GameRepository gameRepository;

    /**
     * 오늘 경기 상세 정보 수집
     */
    public DailyGameData getTodayGameDetails() {
        LocalDate today = LocalDate.now();
        return crawler.getDailyData(today);
    }

    /**
     * 특정 날짜 경기 상세 정보 수집
     */
    public DailyGameData getGameDetails(LocalDate date) {
        return crawler.getDailyData(date);
    }

    public void updateGameStatuses(LocalDate startDate, LocalDate endDate) {
        // 1) 크롤링
        List<LocalDate> dates = getDatesBetweenInclusive(startDate, endDate);
        Map<LocalDate, List<GameInfo>> gamesByDate = crawler.crawlGamesByDate(dates);

        // 2) 날짜+시간으로 기존 경기와 매칭하여 상태 업데이트
        for (Map.Entry<LocalDate, List<GameInfo>> entry : gamesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<GameInfo> crawledGames = entry.getValue();

            // 해당 날짜의 기존 경기 조회
            List<Game> existingGames = gameRepository.findByDate(date);

            for (GameInfo crawled : crawledGames) {
                // 시간 파싱
                LocalTime startTime = parseStartTime(crawled.getStartTime());

                // 날짜 + 시간으로 매칭
                existingGames.stream()
                        .filter(game -> matchByDateTime(game, date, startTime))
                        .findFirst()
                        .ifPresent(game -> {
                            GameState newState = mapper.toState(crawled.getStatus());
                            game.updateGameState(newState);
                            log.info("경기 상태 업데이트: {} {} -> {}",
                                    date, startTime, newState);
                        });
            }
        }

        // 3) 일괄 저장
        gameRepository.flush();
    }

    private boolean matchByDateTime(Game game, LocalDate date, LocalTime startTime) {
        if (!game.getDate().equals(date)) {
            return false;
        }

        if (startTime == null || game.getStartAt() == null) {
            return false;
        }

        return game.getStartAt().equals(startTime);
    }

    private LocalTime parseStartTime(String timeText) {
        if (timeText == null || timeText.isBlank()) {
            return null;
        }

        try {
            // "18:30" 형태
            return LocalTime.parse(timeText.trim());
        } catch (Exception e) {
            try {
                // "1830" 형태
                String digits = timeText.replaceAll("[^0-9]", "");
                if (digits.length() >= 4) {
                    int hour = Integer.parseInt(digits.substring(0, 2));
                    int minute = Integer.parseInt(digits.substring(2, 4));
                    return LocalTime.of(hour, minute);
                }
            } catch (Exception ignored) {
            }

            log.warn("시간 파싱 실패: {}", timeText);
            return null;
        }
    }

    private List<LocalDate> getDatesBetweenInclusive(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }
}
