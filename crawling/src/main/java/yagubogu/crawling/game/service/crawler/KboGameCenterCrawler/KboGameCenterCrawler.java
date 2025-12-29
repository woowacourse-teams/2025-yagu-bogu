package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.microsoft.playwright.Locator;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.GameCenterDetail;
import yagubogu.crawling.game.service.crawler.page.KboGameCenterPage;

@Slf4j
public class KboGameCenterCrawler {

    private final KboCrawlerProperties properties;
    private final PlaywrightManager playwrightManager;

    public KboGameCenterCrawler(KboCrawlerProperties properties, PlaywrightManager playwrightManager) {
        this.properties = properties;
        this.playwrightManager = playwrightManager;
    }

    /**
     * 일일 경기 상세 정보 크롤링
     */
    public GameCenter fetchDailyGameCenter(LocalDate date) {
        return playwrightManager.withPage(page -> {
            KboGameCenterPage gameCenterPage = new KboGameCenterPage(page, properties);
            return fetchGameCenterData(gameCenterPage, date);
        });
    }

    private GameCenter fetchGameCenterData(KboGameCenterPage gameCenterPage, LocalDate date) {
        GameCenter dailyData = new GameCenter();

        try {
            // 페이지 이동
            gameCenterPage.navigateTo();
            gameCenterPage.navigateToDate(date);

            // 날짜 정보 추출
            String dateFormatted = gameCenterPage.getDateText();
            dailyData.setDate(dateFormatted);

            // 경기 목록 수집
            int gameCount = gameCenterPage.getGameCount();

            if (gameCount == 0) {
                log.info("오늘 경기가 없습니다.");
                return dailyData;
            }

            log.info("총 {}경기 정보 수집 시작", gameCount);

            // 각 경기 처리
            for (int i = 0; i < gameCount; i++) {
                processGame(gameCenterPage, dailyData, dateFormatted, i, gameCount);
            }

            log.info("일일 경기 정보 수집 완료");

        } catch (Exception e) {
            log.error("일일 데이터 크롤링 실패", e);
        }

        return dailyData;
    }

    private void processGame(
            KboGameCenterPage gameCenterPage,
            GameCenter dailyData,
            String dateFormatted,
            int index,
            int totalCount) {
        try {
            log.info("경기 {}/{} 처리 중...", index + 1, totalCount);

            Locator gameElement = gameCenterPage.getGameElement(index);
            GameCenterDetail gameDetail = gameCenterPage.extractGameDetail(gameElement, dateFormatted);

            if (gameDetail != null) {
                dailyData.addGameDetail(gameDetail);
            }

        } catch (Exception e) {
            log.error("경기 {} 처리 실패: {}", index + 1, e.getMessage());
        }
    }
}
