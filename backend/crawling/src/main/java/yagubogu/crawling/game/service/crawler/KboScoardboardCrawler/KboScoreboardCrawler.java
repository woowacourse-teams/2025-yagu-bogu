package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.PlaywrightException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.service.crawler.page.KboScoreboardPage;

@Slf4j
@RequiredArgsConstructor
public class KboScoreboardCrawler {

    private final KboCrawlerProperties properties;
    private final PlaywrightManager pwManager;

    public synchronized Map<LocalDate, List<KboScoreboardGame>> crawl(List<LocalDate> dates) {
        Map<LocalDate, List<KboScoreboardGame>> result = new LinkedHashMap<>();
        if (dates == null || dates.isEmpty()) {
            return result;
        }

        for (LocalDate date : dates) {
            log.debug("조회 날짜: {}", date);

            int maxRetries = 3;
            boolean success = false;

            for (int attempt = 1; attempt <= maxRetries && !success; attempt++) {
                try {
                    List<KboScoreboardGame> games = pwManager.withPage(page -> {
                        KboScoreboardPage scoreboardPage = new KboScoreboardPage(page, properties);
                        return fetchScoreboardData(scoreboardPage, date);
                    });

                    result.put(date, games);
                    success = true;

                } catch (PlaywrightException e) {
                    log.warn("날짜 {} 크롤링 실패 (시도 {}/{}): {}", date, attempt, maxRetries, e.getMessage());

                    if (attempt < maxRetries) {
                        sleepQuietly(2000);
                    }
                }
            }

            if (!success) {
                log.error("❌ 날짜 {} 크롤링 최종 실패 - 해당 날짜 데이터 없음", date);
            }
        }

        return result;
    }

    private List<KboScoreboardGame> fetchScoreboardData(KboScoreboardPage scoreboardPage, LocalDate date) {
        scoreboardPage.navigateTo();
        scoreboardPage.navigateToDate(date);

        if (!scoreboardPage.hasScoreboards()) {
            log.info("스코어보드가 존재하지 않습니다.");
            return List.of();
        }

        List<ElementHandle> scoreboards = scoreboardPage.getScoreboards();
        if (scoreboards.isEmpty()) {
            return List.of();
        }

        List<KboScoreboardGame> games = new ArrayList<>();
        for (ElementHandle scoreboard : scoreboards) {
            Optional<KboScoreboardGame> parsed = scoreboardPage.parseScoreboard(scoreboard, date);
            parsed.ifPresent(games::add);
        }

        return games;
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }
}
