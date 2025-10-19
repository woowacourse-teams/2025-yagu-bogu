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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.PlaywrightManager;
import yagubogu.crawling.game.dto.GameInfo;
import yagubogu.crawling.game.dto.PitcherDetailInfo;
import yagubogu.crawling.game.dto.TeamDetailInfo;

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
                        .collect(Collectors.toList());

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
    public DailyGameData getDailyData(LocalDate date) {
        return playwrightManager.withPage(page -> {
            DailyGameData dailyData = new DailyGameData();

            try {
                navigateToUrl(page);
                navigateToDate(page, date);

                page.waitForTimeout(3000);

                String dateText = page.locator("#lblGameDate").textContent();
                String dateFormatted = dateText.substring(0, 10).replace(".", "-");
                dailyData.setDate(dateFormatted);

                // Locator 사용 (더 현대적)
                Locator gameLocator = page.locator(".game-cont");
                int gameCount = gameLocator.count();

                if (gameCount == 0) {
                    log.info("오늘 경기가 없습니다.");
                    return dailyData;
                }

                log.info("총 {}경기 크롤링 시작", gameCount);

                for (int i = 0; i < gameCount; i++) {
                    try {
                        log.info("경기 {}/{} 처리 중...", i + 1, gameCount);

                        // Locator로 클릭 (자동으로 재시도)
                        gameLocator.nth(i).click(new Locator.ClickOptions()
                                .setForce(true)
                                .setTimeout(10000)
                        );

                        page.waitForTimeout(2000);

                        List<PitcherDetailInfo> pitchers = collectPitcherInfo(page, dateFormatted);
                        dailyData.addPitchers(pitchers);

                        List<TeamDetailInfo> teams = collectTeamInfo(page, dateFormatted);
                        dailyData.addTeams(teams);

                    } catch (Exception e) {
                        log.error("경기 {} 처리 실패: {}", i + 1, e.getMessage());
                    }
                }

                log.info("일일 경기 정보 크롤링 성공");

            } catch (Exception e) {
                log.error("일일 데이터 크롤링 실패", e);
            }

            return dailyData;
        });
    }

    /**
     * bx-loading이 사라질 때까지 대기
     */
    private void waitForBxLoading(Page page) {
        try {
            // bx-loading이 숨겨질 때까지 대기
            page.waitForFunction(
                    "document.querySelector('.bx-loading') === null || " +
                            "getComputedStyle(document.querySelector('.bx-loading')).display === 'none'"
            );

            log.debug("bx-loading 사라짐");

        } catch (TimeoutError e) {
            log.warn("bx-loading 대기 시간 초과 (무시)");
        }

        // 추가 안정화 대기
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

            // 달력 버튼 클릭
            page.click(".ui-datepicker-trigger", new Page.ClickOptions()
                    .setTimeout(5000)
            );

            page.waitForSelector(".ui-datepicker",
                    new Page.WaitForSelectorOptions()
                            .setTimeout(3000)
                            .setState(WaitForSelectorState.VISIBLE)
            );

            page.waitForTimeout(500);

            selectYearMonth(page, date);
            clickDay(page, date);

            // 데이터 로딩 및 bx-loading 대기
            page.waitForTimeout(2000);

            // bx-loading 사라질 때까지 대기
            try {
                page.waitForFunction(
                        "document.querySelector('.bx-loading') === null || " +
                                "window.getComputedStyle(document.querySelector('.bx-loading')).display === 'none'"
                );
            } catch (TimeoutError e) {
                log.warn("bx-loading 대기 시간 초과");
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
     * 선발투수 정보 수집
     */
    private List<PitcherDetailInfo> collectPitcherInfo(Page page, String date) {
        List<PitcherDetailInfo> pitchers = new ArrayList<>();

        try {
            // 1. 시즌 기록
            List<ElementHandle> seasonRows = page.querySelectorAll("#tblStartPitcher > tbody > tr");

            if (seasonRows.isEmpty()) {
                return pitchers;
            }

            PitcherDetailInfo awayPitcher = parsePitcherRow(seasonRows.get(0), date, "어웨이");
            PitcherDetailInfo homePitcher = parsePitcherRow(seasonRows.get(1), date, "홈");

            // 2. 홈/어웨이 기록
            page.click("xpath=//*[@id='gameCenterContents']/div[2]/ul/li[2]/a");
            page.waitForTimeout(500);

            List<ElementHandle> haRows = page.querySelectorAll("#tblStartPitcher > tbody > tr");
            parseHARecord(haRows.get(0), awayPitcher);
            parseHARecord(haRows.get(1), homePitcher);

            // 3. 맞대결 기록
            page.click("xpath=//*[@id='gameCenterContents']/div[2]/ul/li[3]/a");
            page.waitForTimeout(500);

            List<ElementHandle> vsRows = page.querySelectorAll("#tblStartPitcher > tbody > tr");
            parseVSRecord(vsRows.get(0), awayPitcher);
            parseVSRecord(vsRows.get(1), homePitcher);

            pitchers.add(awayPitcher);
            pitchers.add(homePitcher);

        } catch (Exception e) {
            log.error("선발투수 정보 수집 실패: {}", e.getMessage());
        }

        return pitchers;
    }

    /**
     * 팀 정보 수집
     */
    private List<TeamDetailInfo> collectTeamInfo(Page page, String date) {
        List<TeamDetailInfo> teams = new ArrayList<>();

        try {
            // 팀 전력비교 메뉴로 이동
            page.evaluate("setGameDetailSection('TEAM')");
            page.waitForTimeout(1000);

            // 1. 시즌 기록
            List<ElementHandle> seasonRows = page.querySelectorAll("#tblRecord > tbody > tr");

            TeamDetailInfo awayTeam = parseTeamRow(seasonRows.get(0), date, "어웨이");
            TeamDetailInfo homeTeam = parseTeamRow(seasonRows.get(1), date, "홈");

            // 2. 홈/어웨이 기록
            page.click("xpath=//*[@id='gameCenterContents']/div[2]/ul/li[2]/a");
            page.waitForTimeout(500);

            List<ElementHandle> haRows = page.querySelectorAll("#tblRecord > tbody > tr");
            parseTeamHARecord(haRows.get(0), awayTeam);
            parseTeamHARecord(haRows.get(1), homeTeam);

            // 3. 맞대결 기록
            page.click("xpath=//*[@id='gameCenterContents']/div[2]/ul/li[3]/a");
            page.waitForTimeout(500);

            List<ElementHandle> vsRows = page.querySelectorAll("#tblRecord > tbody > tr");
            parseTeamVSRecord(vsRows.get(0), awayTeam);
            parseTeamVSRecord(vsRows.get(1), homeTeam);

            teams.add(awayTeam);
            teams.add(homeTeam);

        } catch (Exception e) {
            log.error("팀 정보 수집 실패: {}", e.getMessage());
        }

        return teams;
    }

    /**
     * 투수 행 파싱 (시즌 기록)
     */
    private PitcherDetailInfo parsePitcherRow(ElementHandle row, String date, String homeAway) {
        PitcherDetailInfo pitcher = new PitcherDetailInfo();
        pitcher.setDate(date);
        pitcher.setHomeAway(homeAway);

        pitcher.setPitcherName(row.querySelector(".name").textContent().trim());

        List<ElementHandle> cells = row.querySelectorAll("td");
        pitcher.setSeasonEra(cells.get(1).textContent().trim());
        pitcher.setSeasonWar(cells.get(2).textContent().trim());
        pitcher.setSeasonGames(cells.get(3).textContent().trim());
        pitcher.setSeasonAvgInning(cells.get(4).textContent().trim());
        pitcher.setSeasonQs(cells.get(5).textContent().trim());
        pitcher.setSeasonWhip(cells.get(6).textContent().trim());

        return pitcher;
    }

    /**
     * 홈/어웨이 기록 파싱 (투수)
     */
    private void parseHARecord(ElementHandle row, PitcherDetailInfo pitcher) {
        List<ElementHandle> cells = row.querySelectorAll("td");
        pitcher.setHaEra(cells.get(1).textContent().trim());
        pitcher.setHaGames(cells.get(2).textContent().trim());
        pitcher.setHaAvgInning(cells.get(3).textContent().trim());
        pitcher.setHaQs(cells.get(4).textContent().trim());
        pitcher.setHaWhip(cells.get(5).textContent().trim());
    }

    /**
     * 맞대결 기록 파싱 (투수)
     */
    private void parseVSRecord(ElementHandle row, PitcherDetailInfo pitcher) {
        List<ElementHandle> cells = row.querySelectorAll("td");
        pitcher.setVsEra(cells.get(1).textContent().trim());
        pitcher.setVsGames(cells.get(2).textContent().trim());
        pitcher.setVsAvgInning(cells.get(3).textContent().trim());
        pitcher.setVsQs(cells.get(4).textContent().trim());
        pitcher.setVsWhip(cells.get(5).textContent().trim());
    }

    /**
     * 팀 행 파싱 (시즌 기록)
     */
    private TeamDetailInfo parseTeamRow(ElementHandle row, String date, String homeAway) {
        TeamDetailInfo team = new TeamDetailInfo();
        team.setDate(date);
        team.setHomeAway(homeAway);

        team.setTeamName(row.querySelector("th > span").textContent().trim());

        List<ElementHandle> cells = row.querySelectorAll("td");
        team.setSeasonEra(cells.get(2).textContent().trim());
        team.setSeasonAvg(cells.get(3).textContent().trim());
        team.setSeasonAvgScore(cells.get(4).textContent().trim());
        team.setSeasonAvgLost(cells.get(5).textContent().trim());

        return team;
    }

    /**
     * 홈/어웨이 기록 파싱 (팀)
     */
    private void parseTeamHARecord(ElementHandle row, TeamDetailInfo team) {
        List<ElementHandle> cells = row.querySelectorAll("td");
        team.setHaEra(cells.get(2).textContent().trim());
        team.setHaAvg(cells.get(3).textContent().trim());
        team.setHaAvgScore(cells.get(4).textContent().trim());
        team.setHaAvgLost(cells.get(5).textContent().trim());
    }

    /**
     * 맞대결 기록 파싱 (팀)
     */
    private void parseTeamVSRecord(ElementHandle row, TeamDetailInfo team) {
        List<ElementHandle> cells = row.querySelectorAll("td");
        team.setVsEra(cells.get(2).textContent().trim());
        team.setVsAvg(cells.get(3).textContent().trim());
        team.setVsAvgScore(cells.get(4).textContent().trim());
        team.setVsAvgLost(cells.get(5).textContent().trim());
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
