package yagubogu.crawling.game.service.crawler.KboGameCenterCrawler;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.GameDetailInfo;
import yagubogu.crawling.game.dto.GameInfo;

@Slf4j
public class KboGameCenterCrawler {

    private static final String KBO_URL = "https://www.koreabaseball.com/Schedule/GameCenter/Main.aspx";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PlaywrightManager playwrightManager;
    private final Duration navigationTimeout;
    private final Duration waitTimeout;

    public KboGameCenterCrawler(
            PlaywrightManager playwrightManager,
            Duration navigationTimeout,
            Duration waitTimeout) {
        this.playwrightManager = playwrightManager;
        this.navigationTimeout = navigationTimeout;
        this.waitTimeout = waitTimeout;
    }

    /**
     * 여러 날짜의 경기 정보를 한 번에 크롤링
     */
    public Map<LocalDate, List<GameInfo>> crawlGamesByDate(List<LocalDate> dates) {
        return playwrightManager.withPage(page -> {
            Map<LocalDate, List<GameInfo>> gamesByDate = new LinkedHashMap<>();

            if (dates == null || dates.isEmpty()) {
                log.info("조회할 날짜가 없습니다.");
                return gamesByDate;
            }

            try {
                // 페이지로 이동
                navigateToUrl(page);

                // 날짜 순서대로 정렬
                List<LocalDate> sortedDates = dates.stream()
                        .sorted()
                        .toList();

                log.info("총 {}개 날짜 크롤링 시작", sortedDates.size());

                for (int i = 0; i < sortedDates.size(); i++) {
                    LocalDate date = sortedDates.get(i);
                    log.info("조회 날짜: {} ({}/{})", date, i + 1, sortedDates.size());

                    try {
                        List<GameInfo> games = crawlSingleDate(page, date);
                        gamesByDate.put(date, games);

                        if (i < sortedDates.size() - 1) {
                            Thread.sleep(1000);
                        }

                    } catch (Exception e) {
                        log.error("날짜 {} 크롤링 실패: {}", date, e.getMessage());
                        gamesByDate.put(date, new ArrayList<>());
                    }
                }

                log.info("크롤링 완료!");
                printSummary(gamesByDate);

            } catch (Exception e) {
                log.error("크롤링 전체 실패", e);
            }

            return gamesByDate;
        });
    }

    /**
     * 일일 경기 상세 정보 크롤링
     */
    /**
     * 일일 경기 상세 정보 크롤링 (클릭 없이 목록에서 수집)
     */
    public DailyGameData getDailyData(LocalDate date) {
        return playwrightManager.withPage(page -> {
            DailyGameData dailyData = new DailyGameData();

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
                        GameDetailInfo gameDetail = extractGameDetailFromElement(currentGame, dateFormatted);

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
    private GameDetailInfo extractGameDetailFromElement(Locator gameElement, String date) {
        try {
            GameDetailInfo detail = new GameDetailInfo();
            detail.setDate(date);

            // li 태그의 속성들
            detail.setGameId(gameElement.getAttribute("g_id"));
            detail.setGameDate(gameElement.getAttribute("g_dt"));
            detail.setGameSc(gameElement.getAttribute("game_sc"));
            detail.setAwayTeamId(gameElement.getAttribute("away_id"));
            detail.setHomeTeamId(gameElement.getAttribute("home_id"));
            detail.setAwayTeamName(gameElement.getAttribute("away_nm"));
            detail.setHomeTeamName(gameElement.getAttribute("home_nm"));
            detail.setStadium(gameElement.getAttribute("s_nm"));

            // 경기 상태
            String classAttr = gameElement.getAttribute("class");
            if (classAttr != null) {
                if (classAttr.contains("end")) {
                    detail.setGameStatus("경기종료");
                } else if (classAttr.contains("cancel")) {
                    detail.setGameStatus("경기취소");
                } else {
                    detail.setGameStatus("경기예정");
                }
            }

            // top 영역: 경기장, 날씨, 시간
            Locator topItems = gameElement.locator(".top > ul > li");
            int topCount = topItems.count();

            if (topCount >= 1) {
                detail.setStadiumName(topItems.nth(0).textContent().trim());
            }

            // 날씨 이미지 (있을 경우)
            if (topCount >= 2) {
                Locator weatherImg = topItems.nth(1).locator("img");
                if (weatherImg.count() > 0) {
                    detail.setWeatherIcon(weatherImg.getAttribute("src"));
                }
            }

            // 경기 시간
            if (topCount >= 3) {
                detail.setStartTime(topItems.nth(topCount - 1).textContent().trim());
            } else if (topCount == 2) {
                // 날씨 없는 경우
                detail.setStartTime(topItems.nth(1).textContent().trim());
            }

            // middle 영역: 중계, 상태, 점수, 투수
            Locator broadcastingElem = gameElement.locator(".middle .broadcasting");
            if (broadcastingElem.count() > 0) {
                detail.setBroadcasting(broadcastingElem.textContent().trim());
            }

            Locator statusElem = gameElement.locator(".middle .staus");
            if (statusElem.count() > 0) {
                detail.setStatus(statusElem.textContent().trim());
            }

            // 어웨이 팀 정보
            Locator awayTeam = gameElement.locator(".team.away");
            if (awayTeam.count() > 0) {
                // 점수
                Locator awayScore = awayTeam.locator(".score");
                if (awayScore.count() > 0) {
                    String scoreText = awayScore.textContent().trim();
                    detail.setAwayScore(scoreText);

                    // 승리 여부
                    String scoreClass = awayScore.getAttribute("class");
                    if (scoreClass != null && scoreClass.contains("win")) {
                        detail.setWinner("away");
                    }
                }

                // 투수 정보
                Locator awayPitchers = awayTeam.locator(".today-pitcher p");
                List<String> awayPitcherList = new ArrayList<>();
                for (int i = 0; i < awayPitchers.count(); i++) {
                    String pitcherInfo = awayPitchers.nth(i).textContent().trim();
                    awayPitcherList.add(pitcherInfo);
                }
                detail.setAwayPitchers(awayPitcherList);
            }

            // 홈 팀 정보
            Locator homeTeam = gameElement.locator(".team.home");
            if (homeTeam.count() > 0) {
                // 점수
                Locator homeScore = homeTeam.locator(".score");
                if (homeScore.count() > 0) {
                    String scoreText = homeScore.textContent().trim();
                    detail.setHomeScore(scoreText);

                    // 승리 여부
                    String scoreClass = homeScore.getAttribute("class");
                    if (scoreClass != null && scoreClass.contains("win")) {
                        detail.setWinner("home");
                    }
                }

                // 투수 정보
                Locator homePitchers = homeTeam.locator(".today-pitcher p");
                List<String> homePitcherList = new ArrayList<>();
                for (int i = 0; i < homePitchers.count(); i++) {
                    String pitcherInfo = homePitchers.nth(i).textContent().trim();
                    homePitcherList.add(pitcherInfo);
                }
                detail.setHomePitchers(homePitcherList);
            }

            log.info("경기 정보 수집 완료: {} vs {} ({})",
                    detail.getAwayTeamName(), detail.getHomeTeamName(), detail.getStatus());

            return detail;

        } catch (Exception e) {
            log.error("경기 정보 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * bx-loading이 사라질 때까지 대기
     */
    private void waitForBxLoading(Page page) {
        try {
            // bx-loading이 숨겨질 때까지 대기
            Locator loadingLocator = page.locator(".bx-loading");

            // 요소가 숨겨지길 대기
            loadingLocator.waitFor(new Locator.WaitForOptions()
                    .setState(WaitForSelectorState.HIDDEN)
                    .setTimeout(10000)
            );

            log.debug("bx-loading 사라짐");

        } catch (TimeoutError e) {
            log.warn("bx-loading 대기 시간 초과");
        }

        page.waitForTimeout(500);
    }

    /**
     * URL로 이동
     */
    private void navigateToUrl(Page page) {
        log.info("KBO 게임센터로 이동 중...");

        page.navigate(KBO_URL, new Page.NavigateOptions()
                .setTimeout(navigationTimeout.toMillis())
                .setWaitUntil(WaitUntilState.NETWORKIDLE)
        );

        // 충분한 대기 시간 (JavaScript 실행 완료)
        page.waitForTimeout(4000);

        log.info("페이지 로딩 완료");
    }


    private void clickDay(Page page, LocalDate date) {
        try {
            int day = date.getDayOfMonth();

            // 방법 1: Locator 사용 (더 안전)
            String selector = String.format(
                    ".ui-datepicker-calendar tbody td:not(.ui-datepicker-other-month) a:has-text('%d')",
                    day
            );

            Locator dayLocator = page.locator(selector);

            // 요소가 있는지 확인
            if (dayLocator.count() > 0) {
                // 클릭 전 대기
                dayLocator.first().waitFor(new Locator.WaitForOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(3000)
                );

                // 클릭
                dayLocator.first().click(new Locator.ClickOptions()
                        .setTimeout(5000)
                        .setForce(false)  // 자연스러운 클릭
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
            page.waitForTimeout(500);

            // 월 선택
            page.selectOption(".ui-datepicker-month",
                    String.valueOf(date.getMonthValue() - 1));
            page.waitForTimeout(800);  // 대기 시간 증가

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
                    new Page.ClickOptions().setTimeout(5000));

            page.waitForSelector(".ui-datepicker",
                    new Page.WaitForSelectorOptions()
                            .setTimeout(3000)
                            .setState(WaitForSelectorState.VISIBLE)
            );

            page.waitForTimeout(500);

            selectYearMonth(page, date);
            clickDay(page, date);

            // 날짜 변경 후 데이터 로딩 대기
            page.waitForTimeout(2000);

            // bx-loading 대기
            waitForBxLoading(page);

            // 경기 목록이 로드될 때까지 대기
            try {
                page.waitForSelector(".game-list-n > li",
                        new Page.WaitForSelectorOptions()
                                .setTimeout(5000)
                                .setState(WaitForSelectorState.ATTACHED)
                );
            } catch (TimeoutError e) {
                log.info("경기 목록 없음 (경기 없는 날)");
            }

            page.waitForTimeout(1000);
            log.info("날짜 이동 완료");

        } catch (Exception e) {
            log.error("날짜 이동 실패", e);
            throw new RuntimeException("날짜 이동 실패", e);
        }
    }

    /**
     * 단일 날짜 크롤링
     */
    private List<GameInfo> crawlSingleDate(Page page, LocalDate date) {
        List<GameInfo> games = new ArrayList<>();

        try {
            page.click(".ui-datepicker-trigger", new Page.ClickOptions().setTimeout(5000));

            page.waitForSelector(".ui-datepicker",
                    new Page.WaitForSelectorOptions().setTimeout(2000));

            selectYearMonth(page, date);
            clickDay(page, date);

            page.waitForTimeout(1500);

            try {
                page.waitForFunction(
                        "document.querySelectorAll('.game-list-n > li').length >= 0",
                        new Page.WaitForFunctionOptions().setTimeout(3000)
                );
            } catch (TimeoutError e) {
                log.info("  → 경기 없음");
                return games;
            }

            List<ElementHandle> gameElements = page.querySelectorAll(".game-list-n > li");

            if (gameElements.isEmpty()) {
                log.info("  → 경기 없음");
                return games;
            }

            for (ElementHandle gameElement : gameElements) {
                GameInfo game = parseGameInfo(gameElement);
                if (game != null) {
                    games.add(game);
                }
            }

            log.info("  → {}경기 수집 완료", games.size());

        } catch (Exception e) {
            log.error("  → 에러: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return games;
    }


    /**
     * 경기 요소에서 정보 추출
     */
    private GameInfo parseGameInfo(ElementHandle gameElement) {
        try {
            GameInfo game = new GameInfo();

            game.setGameId(gameElement.getAttribute("g_id"));
            game.setGameDate(gameElement.getAttribute("g_dt"));
            game.setGameSc(gameElement.getAttribute("game_sc"));
            game.setAwayTeam(gameElement.getAttribute("away_id"));
            game.setHomeTeam(gameElement.getAttribute("home_id"));

            String classAttr = gameElement.getAttribute("class");
            if (classAttr != null) {
                if (classAttr.contains("end")) {
                    game.setGameStatus("END");
                } else if (classAttr.contains("cancel")) {
                    game.setGameStatus("CANCEL");
                } else {
                    game.setGameStatus("SCHEDULED");
                }
            }

            ElementHandle stadiumElement = gameElement.querySelector(".top > ul > li:nth-child(1)");
            if (stadiumElement != null) {
                game.setStadium(stadiumElement.textContent().trim());
            }

            ElementHandle timeElement = gameElement.querySelector(".top > ul > li:nth-child(2)");
            if (timeElement != null) {
                game.setStartTime(timeElement.textContent().trim());
            }

            ElementHandle statusElement = gameElement.querySelector(".staus");
            if (statusElement != null) {
                game.setStatus(statusElement.textContent().trim());
            }

            List<ElementHandle> scoreElements = gameElement.querySelectorAll(".score");
            if (scoreElements.size() >= 2) {
                String awayScore = scoreElements.get(0).textContent().trim();
                String homeScore = scoreElements.get(1).textContent().trim();

                if (awayScore.matches("\\d+")) {
                    game.setAwayScore(awayScore);
                }
                if (homeScore.matches("\\d+")) {
                    game.setHomeScore(homeScore);
                }
            }

            return game;

        } catch (Exception e) {
            log.error("경기 정보 파싱 에러: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 크롤링 결과 요약 출력
     */
    private void printSummary(Map<LocalDate, List<GameInfo>> gamesByDate) {
        int totalGames = gamesByDate.values().stream()
                .mapToInt(List::size)
                .sum();

        long datesWithGames = gamesByDate.values().stream()
                .filter(list -> !list.isEmpty())
                .count();

        long endedGames = gamesByDate.values().stream()
                .flatMap(List::stream)
                .filter(game -> "END".equals(game.getGameStatus()))
                .count();

        long canceledGames = gamesByDate.values().stream()
                .flatMap(List::stream)
                .filter(game -> "CANCEL".equals(game.getGameStatus()))
                .count();

        long scheduledGames = gamesByDate.values().stream()
                .flatMap(List::stream)
                .filter(game -> "SCHEDULED".equals(game.getGameStatus()))
                .count();

        log.info("\n=== 크롤링 요약 ===");
        log.info("조회 날짜 수: {}", gamesByDate.size());
        log.info("경기 있는 날짜: {}", datesWithGames);
        log.info("총 경기 수: {}", totalGames);
        log.info("  - 종료: {}", endedGames);
        log.info("  - 취소: {}", canceledGames);
        log.info("  - 예정: {}", scheduledGames);
    }
}
