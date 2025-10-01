package com.yagubogu.game.service.crawler.KboScheduleCrawler;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.ElementHandle;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Playwright를 사용하여 KBO 일정 페이지를 크롤링하는 유틸리티 클래스입니다.
 */
public class KboScheduleCrawler {

    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger(KboScheduleCrawler.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter KBO_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM.dd", Locale.KOREA);
    private static final Map<String, List<String>> SERIES_IDS = Map.of(
            "regular", List.of("0", "9", "6"),
            "postseason", List.of("3", "4", "5", "7"),
            "trial", List.of("1")
    );

    private final Duration navigationTimeout;
    private final Duration tableTimeout;
    private final int maxRetries;
    private final Duration retryDelay;

    public KboScheduleCrawler() {
        this(Duration.ofSeconds(60), Duration.ofSeconds(30), 3, Duration.ofSeconds(2));
    }

    public KboScheduleCrawler(final Duration navigationTimeout,
                              final Duration tableTimeout,
                              final int maxRetries,
                              final Duration retryDelay) {
        this.navigationTimeout = Objects.requireNonNull(navigationTimeout, "navigationTimeout");
        this.tableTimeout = Objects.requireNonNull(tableTimeout, "tableTimeout");
        this.maxRetries = maxRetries;
        this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
    }

    public List<KboGame> crawlKboSchedule(final LocalDate startDate,
                                          final LocalDate endDate,
                                          final String scheduleType) {
        return crawlKboSchedule(startDate, endDate, scheduleType, DEFAULT_LOGGER);
    }

    public List<KboGame> crawlKboSchedule(final LocalDate startDate,
                                          final LocalDate endDate,
                                          final String scheduleType,
                                          final Logger logger) {
        Logger log = Optional.ofNullable(logger).orElse(DEFAULT_LOGGER);

        if (!SERIES_IDS.containsKey(scheduleType)) {
            log.error("알 수 없는 시즌 타입: {}", scheduleType);
            return List.of();
        }

        String seriesParam = String.join(",", SERIES_IDS.get(scheduleType));
        String url = "https://www.koreabaseball.com/Schedule/Schedule.aspx?seriesId=" + seriesParam;
        log.info("{} ~ {} {} 시즌 데이터 크롤링 시작", DATE_FORMAT.format(startDate), DATE_FORMAT.format(endDate), scheduleType);
        log.info("접속 중: {}", url);

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (Playwright playwright = Playwright.create();
                 Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true))) {
                Page page = browser.newPage();
                page.navigate(url, new Page.NavigateOptions().setTimeout(navigationTimeout.toMillis()));
                return extractSchedule(page, startDate, endDate, scheduleType, log);
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
                                          final String scheduleType,
                                          final Logger logger) {
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
                logger.info("선택된 연도: {}, 선택된 월: {} ({} 시즌)", year, String.format("%02d", monthValue), scheduleType);
            } catch (PlaywrightException exception) {
                logger.warn("{}년 {}월 {} 시즌 선택 실패: {}", year, monthValue, scheduleType, exception.getMessage());
                month = month.plusMonths(1);
                continue;
            }

            try {
                page.waitForSelector(
                        ".tbl-type06",
                        new Page.WaitForSelectorOptions()
                                .setTimeout(tableTimeout.toMillis())
                                .setState(WaitForSelectorState.VISIBLE)
                );
                // 테이블이 이미 보이는 상태일 수 있으므로 약간의 대기 후 파싱
                sleep(retryDelay);
                logger.info("{}년 {}월 {} 시즌 테이블 로드 완료", year, monthValue, scheduleType);
            } catch (PlaywrightException exception) {
                logger.warn("테이블 로드 대기 중 오류: {}", exception.getMessage());
                month = month.plusMonths(1);
                continue;
            }

            List<ElementHandle> rows = page.querySelectorAll(".tbl-type06 tr");
            String previousDate = "";
            for (ElementHandle row : rows) {
                List<String> columns = row.querySelectorAll("td").stream()
                        .map(cell -> cell.innerText().trim())
                        .collect(Collectors.toList());

                if (columns.isEmpty()) {
                    continue;
                }

                if (columns.size() == 9) {
                    previousDate = columns.get(0);
                } else if (columns.size() == 8) {
                    columns.add(0, previousDate);
                }

                if (columns.size() < 9) {
                    logger.warn("예상치 못한 데이터 형식: {}", columns);
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
                    logger.warn("잘못된 날짜 형식: {}", rawDate);
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
                    logger.warn("잘못된 팀 정보 형식: {}", gameInfo);
                    continue;
                }

                ParsedTeam team1 = parseTeamAndScore(teams[0]);
                ParsedTeam team2 = parseTeamAndScore(teams[1]);
                if (team1 == null || team2 == null) {
                    logger.warn("팀 파싱 실패: {}", gameInfo);
                    continue;
                }

                boolean isCancelled = !"-".equals(note.trim());
                String cancelReason = isCancelled ? note : "-";
                String team1Score = team1.score;
                String team2Score = team2.score;
                String result = "-";

                if (isCancelled) {
                    team1Score = "-";
                    team2Score = "-";
                } else if (!"-".equals(team1Score) && !"-".equals(team2Score)) {
                    int homeScore = Integer.parseInt(team1Score);
                    int awayScore = Integer.parseInt(team2Score);
                    if (homeScore > awayScore) {
                        result = "0";
                    } else if (homeScore < awayScore) {
                        result = "1";
                    } else {
                        result = "2";
                    }
                }

                KboGame game = new KboGame(
                        gameDate,
                        gameTime,
                        team1.name,
                        team1Score,
                        team2.name,
                        team2Score,
                        result,
                        stadium,
                        isCancelled,
                        cancelReason,
                        scheduleType,
                        tv
                );
                games.add(game);
            }

            month = month.plusMonths(1);
        }

        // 날짜/팀 기준으로 더블헤더 순서 부여
        applyDoubleHeaderOrder(games);
        return games;
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
}
