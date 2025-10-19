package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.GameCenterDetail;

@Slf4j
public class KboGameCenterCrawler {

    private final String baseUrl;
    private final PlaywrightManager playwrightManager;
    private final Duration navigationTimeout;

    public KboGameCenterCrawler(
            String baseUrl,
            Duration navigationTimeout,
            PlaywrightManager playwrightManager) {
        this.baseUrl = baseUrl;
        this.navigationTimeout = navigationTimeout;
        this.playwrightManager = playwrightManager;
    }

    /**
     * 일일 경기 상세 정보 크롤링 (클릭 없이 목록에서 수집)
     */
    public GameCenter fetchDailyGameCenter(LocalDate date) {
        return playwrightManager.withPage(page -> {
            GameCenter dailyData = new GameCenter();

            try {
                navigateToUrl(page);
                navigateToDate(page, date);
                page.waitForTimeout(3000);

                // 날짜 정보
                String dateText = page.locator("#lblGameDate").textContent();
                String dateFormatted = dateText.substring(0, 10).replace(".", "-");
                dailyData.setDate(dateFormatted);

                // 경기 목록 수집
                Locator gameLocator = page.locator(".game-list-n > li.game-cont");
                int gameCount = gameLocator.count();

                if (gameCount == 0) {
                    log.info("오늘 경기가 없습니다.");
                    return dailyData;
                }

                log.info("총 {}경기 정보 수집 시작", gameCount);

                for (int i = 0; i < gameCount; i++) {
                    try {
                        Locator currentGame = gameLocator.nth(i);

                        log.info("경기 {}/{} 처리 중...", i + 1, gameCount);

                        // game-cont에서 직접 정보 수집
                        GameCenterDetail gameDetail = extractGameDetailFromElement(currentGame, dateFormatted);

                        if (gameDetail != null) {
                            dailyData.addGameDetail(gameDetail);
                        }

                    } catch (Exception e) {
                        log.error("경기 {} 처리 실패: {}", i + 1, e.getMessage());
                    }
                }

                log.info("일일 경기 정보 수집 완료");

            } catch (Exception e) {
                log.error("일일 데이터 크롤링 실패", e);
            }

            return dailyData;
        });
    }

    /**
     * game-cont 요소에서 경기 상세 정보 추출
     */
    private GameCenterDetail extractGameDetailFromElement(Locator gameElement, String date) {
        try {
            GameCenterDetail gameCenter = new GameCenterDetail();
            gameCenter.setDate(date);

            // li 태그의 속성들
            gameCenter.setGameId(gameElement.getAttribute("g_id"));
            gameCenter.setGameDate(gameElement.getAttribute("g_dt"));
            gameCenter.setGameSc(gameElement.getAttribute("game_sc"));
            gameCenter.setAwayTeamCode(gameElement.getAttribute("away_id"));
            gameCenter.setHomeTeamCode(gameElement.getAttribute("home_id"));
            gameCenter.setAwayTeamName(gameElement.getAttribute("away_nm"));
            gameCenter.setHomeTeamName(gameElement.getAttribute("home_nm"));
            gameCenter.setStadium(gameElement.getAttribute("s_nm"));

            // 경기 상태
            String classAttr = gameElement.getAttribute("class");
            if (classAttr != null) {
                if (classAttr.contains("end")) {
                    gameCenter.setGameStatus("경기종료");
                } else if (classAttr.contains("cancel")) {
                    gameCenter.setGameStatus("경기취소");
                } else {
                    gameCenter.setGameStatus("경기예정");
                }
            }

            // top 영역: 경기장, 날씨, 시간
            Locator topItems = gameElement.locator(".top > ul > li");
            int topCount = topItems.count();

            if (topCount >= 1) {
                gameCenter.setStadiumName(topItems.nth(0).textContent().trim());
            }

            // 날씨 이미지 (있을 경우)
            if (topCount >= 2) {
                Locator weatherImg = topItems.nth(1).locator("img");
                if (weatherImg.count() > 0) {
                    gameCenter.setWeatherIcon(weatherImg.getAttribute("src"));
                }
            }

            // 경기 시간
            if (topCount >= 3) {
                gameCenter.setStartTime(topItems.nth(topCount - 1).textContent().trim());
            } else if (topCount == 2) {
                // 날씨 없는 경우
                gameCenter.setStartTime(topItems.nth(1).textContent().trim());
            }

            // middle 영역: 중계, 상태, 점수, 투수
            Locator broadcastingElem = gameElement.locator(".middle .broadcasting");
            if (broadcastingElem.count() > 0) {
                gameCenter.setBroadcasting(broadcastingElem.textContent().trim());
            }

            Locator statusElem = gameElement.locator(".middle .staus");
            if (statusElem.count() > 0) {
                gameCenter.setStatus(statusElem.textContent().trim());
            }

            // 어웨이 팀 정보
            Locator awayTeam = gameElement.locator(".team.away");
            if (awayTeam.count() > 0) {
                // 점수
                Locator awayScore = awayTeam.locator(".score");
                if (awayScore.count() > 0) {
                    String scoreText = awayScore.textContent().trim();
                    gameCenter.setAwayScore(scoreText);

                    // 승리 여부
                    String scoreClass = awayScore.getAttribute("class");
                    if (scoreClass != null && scoreClass.contains("win")) {
                        gameCenter.setWinner("away");
                    }
                }

                // 투수 정보
                Locator awayPitchers = awayTeam.locator(".today-pitcher p");
                List<String> awayPitcherList = new ArrayList<>();
                for (int i = 0; i < awayPitchers.count(); i++) {
                    String pitcherInfo = awayPitchers.nth(i).textContent().trim();
                    awayPitcherList.add(pitcherInfo);
                }
                gameCenter.setAwayPitchers(awayPitcherList);
            }

            // 홈 팀 정보
            Locator homeTeam = gameElement.locator(".team.home");
            if (homeTeam.count() > 0) {
                // 점수
                Locator homeScore = homeTeam.locator(".score");
                if (homeScore.count() > 0) {
                    String scoreText = homeScore.textContent().trim();
                    gameCenter.setHomeScore(scoreText);

                    // 승리 여부
                    String scoreClass = homeScore.getAttribute("class");
                    if (scoreClass != null && scoreClass.contains("win")) {
                        gameCenter.setWinner("home");
                    }
                }

                // 투수 정보
                Locator homePitchers = homeTeam.locator(".today-pitcher p");
                List<String> homePitcherList = new ArrayList<>();
                for (int i = 0; i < homePitchers.count(); i++) {
                    String pitcherInfo = homePitchers.nth(i).textContent().trim();
                    homePitcherList.add(pitcherInfo);
                }
                gameCenter.setHomePitchers(homePitcherList);
            }

            log.info("경기 정보 수집 완료: {} vs {} ({})",
                    gameCenter.getAwayTeamName(), gameCenter.getHomeTeamName(), gameCenter.getStatus());

            return gameCenter;

        } catch (Exception e) {
            log.error("경기 정보 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * URL로 이동
     */
    private void navigateToUrl(Page page) {
        log.info("KBO 게임센터로 이동 중...");

        page.navigate(baseUrl, new Page.NavigateOptions()
                .setTimeout(navigationTimeout.toMillis())
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
        );

        log.info("페이지 로딩 완료");
    }


    private void clickDay(Page page, LocalDate date) {
        try {
            int day = date.getDayOfMonth();

            String selector = String.format(
                    ".ui-datepicker-calendar tbody td:not(.ui-datepicker-other-month) a:has-text('%d')",
                    day
            );

            Locator dayLocator = page.locator(selector);

            // 요소가 있는지 확인
            if (dayLocator.count() > 0) {
                dayLocator.first().click(new Locator.ClickOptions()
                        .setTimeout(2000)
                        .setForce(false)
                );

                log.info("날짜 {} 클릭 완료", day);
                return;
            }

            throw new RuntimeException("날짜를 찾을 수 없습니다: " + day);

        } catch (Exception e) {
            log.error("날짜 클릭 실패", e);
            throw new RuntimeException("날짜 클릭 실패: " + date, e);
        }
    }

    /**
     * 달력에서 년/월 선택 (안전한 버전)
     */
    private void selectYearMonth(Page page, LocalDate date) {
        try {
            // 년도 선택
            page.selectOption(".ui-datepicker-year",
                    String.valueOf(date.getYear()));

            // 월 선택
            page.selectOption(".ui-datepicker-month",
                    String.valueOf(date.getMonthValue() - 1));

            log.info("년/월 선택 완료: {}-{}", date.getYear(), date.getMonthValue());
        } catch (Exception e) {
            log.error("년/월 선택 실패", e);
            throw new RuntimeException("년/월 선택 실패", e);
        }
    }

    /**
     * 특정 날짜로 이동 (안전한 버전)
     */
    private void navigateToDate(Page page, LocalDate date) {
        try {
            log.info("날짜 {}로 이동 중...", date);

            page.click(".ui-datepicker-trigger",
                    new Page.ClickOptions().setTimeout(3000));

            page.waitForSelector(".ui-datepicker",
                    new Page.WaitForSelectorOptions()
                            .setTimeout(1500)
                            .setState(WaitForSelectorState.VISIBLE)
            );

            selectYearMonth(page, date);
            clickDay(page, date);

            // 경기 목록이 로드될 때까지 대기
            try {
                page.waitForSelector(".game-list-n > li",
                        new Page.WaitForSelectorOptions()
                                .setTimeout(1500)
                                .setState(WaitForSelectorState.ATTACHED)
                );
            } catch (TimeoutError e) {
                log.info("경기 목록 없음 (경기 없는 날)");
            }

            log.info("날짜 이동 완료");

        } catch (Exception e) {
            log.error("날짜 이동 실패", e);
            throw new RuntimeException("날짜 이동 실패", e);
        }
    }
}
