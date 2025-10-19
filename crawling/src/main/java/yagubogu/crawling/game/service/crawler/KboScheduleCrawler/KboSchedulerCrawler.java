package yagubogu.crawling.game.service.crawler.KboScheduleCrawler;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KboSchedulerCrawler {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter KBO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM.dd", Locale.KOREA);

    private final Duration navigationTimeout;
    private final Duration tableTimeout;
    private final Duration waitTimeout;
    private final int maxRetries;
    private final Duration retryDelay;

    public KboSchedulerCrawler(final Duration navigationTimeout,
                               final Duration tableTimeout,
                               final Duration waitTimeout,
                               final int maxRetries,
                               final Duration retryDelay) {
        this.navigationTimeout = Objects.requireNonNull(navigationTimeout, "navigationTimeout");
        this.tableTimeout = Objects.requireNonNull(tableTimeout, "tableTimeout");
        this.waitTimeout = waitTimeout;
        this.maxRetries = maxRetries;
        this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
    }

    public List<KboGame> crawlKboSchedule(final LocalDate startDate,
                                          final LocalDate endDate,
                                          final ScheduleType scheduleType) {
        if (ScheduleType.ALL.equals(scheduleType)) {
            return crawlAutoByMonthAndTypes(startDate, endDate);
        }

        String seriesParam = scheduleType.getSeriesParam();
        String url = "https://www.koreabaseball.com/Schedule/Schedule.aspx?seriesId=" + seriesParam;
        log.info("{} ~ {} {} 시즌 데이터 크롤링 시작", DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate), scheduleType);
        log.info("접속 중: {}", url);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout(navigationTimeout.toMillis()));

                return extractSchedule(page, startDate, endDate, scheduleType);
            } catch (PlaywrightException exception) {
                log.error("KBO 스케줄 크롤링 중 오류 발생(시도 {}/{}): {}", attempt, maxRetries, exception.getMessage());
                if (attempt == maxRetries) {
                    break;
                }
                sleep(retryDelay);
            }
        }

        log.info("{} ~ {} {} 시즌 데이터 크롤링 완료(데이터 없음)", DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate),
                scheduleType);

        return List.of();
    }

    private List<KboGame> extractSchedule(final Page page,
                                          final LocalDate startDate,
                                          final LocalDate endDate,
                                          final ScheduleType scheduleType) {
        List<KboGame> games = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);

        YearMonth month = startMonth;
        while (!month.isAfter(endMonth)) {
            int year = month.getYear();
            int monthValue = month.getMonthValue();

            try {
                page.selectOption("#ddlYear", String.valueOf(year));
                page.selectOption("#ddlMonth", String.format("%02d", monthValue));
                log.info("선택된 연도: {}, 선택된 월: {} ({} 시즌)", year, String.format("%02d", monthValue), scheduleType);
            } catch (PlaywrightException exception) {
                log.warn("{}년 {}월 {} 시즌 선택 실패: {}", year, monthValue, scheduleType, exception.getMessage());
                month = month.plusMonths(1);
                continue;
            }

            if (!waitMonthTableOrSkip(page, monthValue)) {
                log.info("{}년 {}월 {} 시즌 데이터 없음", year, monthValue, scheduleType);
                month = month.plusMonths(1);
                continue;
            }

            // 현재 선택된 연/월만 파싱
            games.addAll(parseCurrentMonth(page, year, startDate, endDate, scheduleType));

            month = month.plusMonths(1);
        }

        // 날짜/팀 기준으로 더블헤더 순서 부여
        applyDoubleHeaderOrder(games);
        return games;
    }

    private List<KboGame> parseCurrentMonth(final Page page,
                                            final int year,
                                            final LocalDate startDate,
                                            final LocalDate endDate,
                                            final ScheduleType scheduleType) {
        List<KboGame> result = new ArrayList<>();
        Locator rowsLocator = page.locator(".tbl-type06 tr");
        int rowCount = rowsLocator.count();

        String previousDate = "";

        for (int i = 0; i < rowCount; i++) {
            Locator row = rowsLocator.nth(i);

            // ✅ 각 td의 텍스트를 즉시 추출
            Locator cellsLocator = row.locator("td");
            int cellCount = cellsLocator.count();

            List<String> columns = new ArrayList<>();
            for (int j = 0; j < cellCount; j++) {
                columns.add(cellsLocator.nth(j).innerText().trim());
            }

            if (columns.isEmpty()) {
                continue;
            }

            if (columns.size() == 9) {
                previousDate = columns.getFirst();
            } else if (columns.size() == 8) {
                if (previousDate.isBlank()) {
                    continue;
                }
                columns.addFirst(previousDate);
            }

            if (columns.size() < 9) {
                log.warn("예상치 못한 데이터 형식: {}", columns);
                continue;
            }

            String rawDate = columns.get(0);
            String gameTime = columns.get(1);
            String gameInfo = columns.get(2);
            String tv = columns.get(5);
            String stadium = columns.get(7);
            String note = columns.get(8);

            LocalDate gameDate;
            try {
                String normalized = rawDate.split("\\(")[0].trim();
                gameDate = LocalDate.parse(year + "-" + normalized, KBO_DATE_FORMAT);
            } catch (RuntimeException exception) {
                log.warn("잘못된 날짜 형식: {}", rawDate);
                continue;
            }

            if (gameDate.isBefore(startDate) || gameDate.isAfter(endDate)) {
                continue;
            }

            if (!gameInfo.contains("vs")) {
                continue;
            }

            String[] teams = gameInfo.split("vs");
            if (teams.length != 2) {
                log.warn("잘못된 팀 정보 형식: {}", gameInfo);
                continue;
            }

            ParsedTeam leftTeam = parseTeamAndScore(teams[0]);
            ParsedTeam rightTeam = parseTeamAndScore(teams[1]);
            if (leftTeam == null || rightTeam == null) {
                log.warn("팀 파싱 실패: {}", gameInfo);
                continue;
            }

            boolean isCancelled = !"-".equals(note.trim());
            String cancelReason = isCancelled ? note : "-";
            String leftScore = leftTeam.score;
            String rightScore = rightTeam.score;
            String resultStr = "-";

            if (isCancelled) {
                leftScore = "-";
                rightScore = "-";
            } else if (!"-".equals(leftScore) && !"-".equals(rightScore)) {
                int awayScore = Integer.parseInt(leftScore);
                int homeScore = Integer.parseInt(rightScore);
                if (homeScore > awayScore) {
                    resultStr = "0";
                } else if (homeScore < awayScore) {
                    resultStr = "1";
                } else {
                    resultStr = "2";
                }
            }

            KboGame game = new KboGame(
                    gameDate,
                    gameTime,
                    rightTeam.name,
                    rightScore,
                    leftTeam.name,
                    leftScore,
                    resultStr,
                    stadium,
                    isCancelled,
                    cancelReason,
                    scheduleType,
                    tv
            );
            result.add(game);
        }

        return result;
    }

    private ParsedTeam parseTeamAndScore(final String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        StringBuilder teamBuilder = new StringBuilder();
        StringBuilder scoreBuilder = new StringBuilder();

        for (char ch : raw.toCharArray()) {
            if (Character.isDigit(ch)) {
                scoreBuilder.append(ch);
                continue;
            }
            if (Character.isAlphabetic(ch) || isHangul(ch)) {
                teamBuilder.append(ch);
            } else if (Character.isWhitespace(ch)) {
                teamBuilder.append(' ');
            }
        }

        String teamName = teamBuilder.toString().trim();
        if (teamName.isEmpty()) {
            return null;
        }

        String score = scoreBuilder.length() > 0 ? scoreBuilder.toString() : "-";
        return new ParsedTeam(teamName, score);
    }

    private boolean isHangul(final char ch) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        return block == Character.UnicodeBlock.HANGUL_SYLLABLES
                || block == Character.UnicodeBlock.HANGUL_JAMO
                || block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO;
    }

    private void applyDoubleHeaderOrder(final List<KboGame> games) {
        Map<LocalDate, Map<String, List<KboGame>>> grouped = new HashMap<>();

        for (KboGame game : games) {
            grouped.computeIfAbsent(game.getDate(), ignored -> new HashMap<>())
                    .computeIfAbsent(game.getHomeTeam(), ignored -> new ArrayList<>())
                    .add(game);
        }

        Comparator<String> timeComparator = Comparator.nullsLast(Comparator.naturalOrder());

        for (Map<String, List<KboGame>> byTeam : grouped.values()) {
            for (List<KboGame> teamGames : byTeam.values()) {
                if (teamGames.size() <= 1) {
                    teamGames.forEach(g -> g.setDoubleHeaderGameOrder(-1));
                    continue;
                }

                teamGames.sort(Comparator.comparing(KboGame::getGameTime, timeComparator));
                for (int index = 0; index < teamGames.size(); index++) {
                    teamGames.get(index).setDoubleHeaderGameOrder(index);
                }
            }
        }
    }

    private void sleep(final Duration duration) {
        try {
            TimeUnit.MILLISECONDS.sleep(duration.toMillis());
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private record ParsedTeam(String name, String score) {
    }

    // 지정 월의 테이블 컨테이너가 로드되고, 해당 월의 데이터가 노출되면 true
    // 컨테이너가 없거나 월 접두 데이터가 일정 시간 내 나타나지 않으면 false
    private boolean waitMonthTableOrSkip(final Page page, final int monthValue) {
        try {
            page.waitForSelector(
                    ".tbl-type06",
                    new Page.WaitForSelectorOptions()
                            .setTimeout(tableTimeout.toMillis())
                            .setState(WaitForSelectorState.ATTACHED)
            );
        } catch (PlaywrightException e) {
            log.debug("테이블 컨테이너 없음");
            return false;
        }

        try {
            String monthPrefix = String.format("%02d.", monthValue);
            page.waitForFunction(
                    "prefix => Array.from(document.querySelectorAll('.tbl-type06 tr td:first-child'))\n" +
                            ".map(td => (td.textContent||'').trim()).some(t => t.startsWith(prefix))",
                    monthPrefix,
                    new Page.WaitForFunctionOptions().setTimeout(Math.min(5000, (int) tableTimeout.toMillis()))
            );
            return true;
        } catch (PlaywrightException e) {
            log.debug("해당 월 접두 데이터 미등장");
            return false;
        }
    }

    private List<KboGame> crawlAutoByMonthAndTypes(final LocalDate startDate,
                                                   final LocalDate endDate) {
        log.info("AUTO 모드 시작: {} ~ {}", DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate));

        List<KboGame> all = new ArrayList<>();
        Set<String> dedup = new HashSet<>();

        // ✅ AUTO 모드 전용 Playwright (pwManager 사용 안 함)
        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(
                     new BrowserType.LaunchOptions()
                             .setHeadless(true)
                             .setArgs(List.of(
                                     "--disable-gpu",
                                     "--disable-extensions",
                                     "--no-sandbox",
                                     "--disable-setuid-sandbox"
                             )))) {

            YearMonth startMonth = YearMonth.from(startDate);
            YearMonth endMonth = YearMonth.from(endDate);
            YearMonth month = startMonth;

            while (!month.isAfter(endMonth)) {
                final int m = month.getMonthValue();
                final int year = month.getYear();
                Set<ScheduleType> types = ScheduleType.resolveTypesForMonth(m);

                for (ScheduleType type : types) {
                    if (type == ScheduleType.NONE) {
                        continue;
                    }

                    String seriesParam = String.join(",", type.getSeriesParam());
                    String url = "https://www.koreabaseball.com/Schedule/Schedule.aspx?seriesId=" + seriesParam;
                    log.info("[AUTO] {}월 타입 {} (seriesId={}) 접속: {}", m, type.name(), seriesParam, url);

                    for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        Page page = null;
                        BrowserContext ctx = null;

                        try {
                            // ✅ 매번 새로운 Context/Page 생성
                            ctx = browser.newContext(new Browser.NewContextOptions()
                                    .setViewportSize(1280, 800)
                                    .setUserAgent(
                                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36")
                                    .setBypassCSP(true));

                            page = ctx.newPage();
                            page.setDefaultTimeout(waitTimeout.toMillis());
                            page.setDefaultNavigationTimeout(navigationTimeout.toMillis());

                            page.navigate(url, new Page.NavigateOptions().setTimeout(navigationTimeout.toMillis()));

                            page.selectOption("#ddlYear", String.valueOf(year));
                            page.selectOption("#ddlMonth", String.format("%02d", m));

                            if (!waitMonthTableOrSkip(page, m)) {
                                log.info("[AUTO] {}년 {}월 타입 {} 데이터 없음, 스킵", year, m, type.name());
                                break;
                            }

                            List<KboGame> part = parseCurrentMonth(page, year, startDate, endDate, type);

                            // 중복 제거 후 병합
                            for (KboGame g : part) {
                                String key = dedupKey(g);
                                if (dedup.add(key)) {
                                    all.add(g);
                                }
                            }

                            break; // 성공하면 재시도 루프 탈출

                        } catch (PlaywrightException pe) {
                            log.warn("[AUTO] 크롤링 실패 (월={}, 타입={}, 시도 {}/{}): {}", m, type.name(), attempt, maxRetries,
                                    pe.getMessage());
                            if (attempt == maxRetries) {
                                log.error("[AUTO] 크롤링 포기 (월={}, 타입={})", m, type.name());
                            } else {
                                sleep(retryDelay);
                            }
                        } finally {
                            // ✅ 반드시 정리
                            if (page != null) {
                                try {
                                    page.close();
                                } catch (Exception ignored) {
                                }
                            }
                            if (ctx != null) {
                                try {
                                    ctx.close();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
                month = month.plusMonths(1);
            }

        } catch (PlaywrightException pe) {
            log.error("[AUTO] Playwright 초기화/종료 중 오류: {}", pe.getMessage());
        }

        applyDoubleHeaderOrder(all);
        log.info("AUTO 모드 완료: 총 수집 경기 수 = {}", all.size());
        return all;
    }

    /** 중복 판단 키: 날짜|홈|원정|시간|구장 */
    private String dedupKey(KboGame g) {
        String time = (g.getGameTime() == null || g.getGameTime().isBlank()) ? "-" : g.getGameTime().trim();
        String stadium = (g.getStadium() == null) ? "-" : g.getStadium().trim();
        return g.getDate() + "|" + g.getHomeTeam() + "|" + g.getAwayTeam() + "|" + time + "|" + stadium;
    }
}
