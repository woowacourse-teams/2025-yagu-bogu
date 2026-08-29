package com.yagubogu.admin.service;

import com.yagubogu.admin.client.CrawlingAdminClient;
import com.yagubogu.admin.dto.AdminCrawlingGamesRequest;
import com.yagubogu.admin.dto.AdminCrawlingGamesResponse;
import com.yagubogu.admin.dto.CrawlingGameDateResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminCrawlingService {

    private final CrawlingAdminClient crawlingAdminClient;

    public AdminCrawlingGamesResponse crawlGames(final AdminCrawlingGamesRequest request) {
        long sleepMillis = request.resolvedSleepMillis();
        long reviewRetryDelayMinutes = request.resolvedReviewRetryDelayMinutes();
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();

        int requested = 0;
        int saved = 0;
        int skipped = 0;
        int transformed = 0;
        int reviewSaved = 0;
        int reviewQueued = 0;
        List<String> savedGameCodes = new ArrayList<>();
        List<String> failedGameCodes = new ArrayList<>();
        List<String> failedDates = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            CrawlingGameDateResponse crawlingResponse;
            try {
                crawlingResponse = crawlingAdminClient.fetchGames(date);
                requested += crawlingResponse.requested();
                saved += crawlingResponse.saved();
                skipped += crawlingResponse.skipped();
                transformed += crawlingResponse.transformed();
                savedGameCodes.addAll(crawlingResponse.savedGameCodes());
            } catch (Exception exception) {
                failedDates.add(date.toString());
                log.error("[ADMIN_CRAWLING] Game crawl failed: date={}", date, exception);
                sleep(sleepMillis);
                continue;
            }

            sleep(sleepMillis);

            for (String gameCode : crawlingResponse.completedGameCodes()) {
                try {
                    crawlingAdminClient.fetchReview(gameCode);
                    reviewSaved++;
                } catch (Exception reviewException) {
                    failedGameCodes.add(gameCode);
                    crawlingAdminClient.enqueueReviewRetry(gameCode, reviewRetryDelayMinutes);
                    reviewQueued++;
                    log.warn("[ADMIN_CRAWLING] Review crawl failed, queued retry: gameCode={}", gameCode,
                            reviewException);
                }

                sleep(sleepMillis);
            }
        }

        return new AdminCrawlingGamesResponse(
                requested,
                saved,
                skipped,
                transformed,
                reviewSaved,
                reviewQueued,
                failedDates.size() + failedGameCodes.size(),
                savedGameCodes,
                failedGameCodes,
                failedDates
        );
    }

    private void sleep(final long sleepMillis) {
        if (sleepMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Admin crawling sleep interrupted", exception);
        }
    }
}
