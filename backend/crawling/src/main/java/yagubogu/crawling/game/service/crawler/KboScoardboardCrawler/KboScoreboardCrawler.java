package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.service.crawler.KboHtmlClient;

@Slf4j
public class KboScoreboardCrawler {

    private static final int MAX_RETRIES = 3;
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(2);

    private final KboHtmlClient kboHtmlClient;
    private final KboScoreboardParser scoreboardParser;
    private final Duration retryDelay;

    public KboScoreboardCrawler(
            final KboHtmlClient kboHtmlClient,
            final KboScoreboardParser scoreboardParser) {
        this(kboHtmlClient, scoreboardParser, DEFAULT_RETRY_DELAY);
    }

    public KboScoreboardCrawler(
            final KboHtmlClient kboHtmlClient,
            final KboScoreboardParser scoreboardParser,
            final Duration retryDelay) {
        this.kboHtmlClient = kboHtmlClient;
        this.scoreboardParser = scoreboardParser;
        this.retryDelay = retryDelay;
    }

    public synchronized Map<LocalDate, List<KboScoreboardGame>> crawl(List<LocalDate> dates) {
        Map<LocalDate, List<KboScoreboardGame>> result = new LinkedHashMap<>();
        if (dates == null || dates.isEmpty()) {
            return result;
        }

        for (LocalDate date : dates) {
            log.debug("조회 날짜: {}", date);

            boolean success = false;

            for (int attempt = 1; attempt <= MAX_RETRIES && !success; attempt++) {
                try {
                    List<KboScoreboardGame> games = fetchScoreboardData(date);

                    result.put(date, games);
                    success = true;

                } catch (RuntimeException e) {
                    log.warn("날짜 {} 크롤링 실패 (시도 {}/{}): {}", date, attempt, MAX_RETRIES, e.getMessage());

                    if (attempt < MAX_RETRIES) {
                        sleepQuietly(retryDelay);
                    }
                }
            }

            if (!success) {
                log.error("❌ 날짜 {} 크롤링 최종 실패 - 해당 날짜 데이터 없음", date);
            }
        }

        return result;
    }

    private List<KboScoreboardGame> fetchScoreboardData(final LocalDate date) {
        return scoreboardParser.parse(kboHtmlClient.fetchScoreboard(date), date);
    }

    private void sleepQuietly(final Duration duration) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            return;
        }
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
