package yagubogu.crawling.game.service.crawler.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.yagubogu.game.exception.GameSyncException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;

@Slf4j
public class KboScoreboardPage extends BaseKboPage {

    private final Pattern pitcherPattern;
    private final DateTimeFormatter timeFormatter;

    public KboScoreboardPage(Page page, KboCrawlerProperties properties) {
        super(page, properties);
        this.pitcherPattern = Pattern.compile(properties.getPatterns().getPitcherLabel());
        this.timeFormatter = DateTimeFormatter.ofPattern(properties.getPatterns().getTimeFormat());
    }

    @Override
    protected String getBaseUrl() {
        return properties.getCrawler().getScoreBoardUrl();
    }

    @Override
    protected boolean needsDateChangeValidation() {
        return true;
    }

    @Override
    protected void waitForContentUpdate(long timeout) {
        var calendarSelectors = properties.getSelectors().getCalendar();

        page.locator(calendarSelectors.getUpdatePanel())
                .waitFor(new Locator.WaitForOptions()
                        .setTimeout(timeout)
                        .setState(WaitForSelectorState.ATTACHED));
    }

    // ==================== 데이터 추출 ====================

    /**
     * 스코어보드 존재 여부 확인
     */
    public boolean hasScoreboards() {
        long timeout = properties.getCrawler().getWaitTimeout().toMillis();
        String containerSelector = properties.getSelectors().getScoreboard().getContainer();

        try {
            page.waitForSelector(containerSelector,
                    new Page.WaitForSelectorOptions()
                            .setTimeout(timeout)
                            .setState(WaitForSelectorState.ATTACHED));
            return true;
        } catch (PlaywrightException ignore) {
            return false;
        }
    }

    /**
     * 스코어보드 요소 목록 반환
     */
    public List<ElementHandle> getScoreboards() {
        String containerSelector = properties.getSelectors().getScoreboard().getContainer();
        return page.querySelectorAll(containerSelector);
    }

    /**
     * 스코어보드 파싱
     */
    public Optional<KboScoreboardGame> parseScoreboard(ElementHandle scoreboard, LocalDate date) {
        var selectors = properties.getSelectors().getScoreboard();

        // 기본 정보 추출
        String status = safeTextCSS(scoreboard, selectors.getStatus());
        String stadium = safeTextCSS(scoreboard, selectors.getStadium());
        String startTime = safeTextCSS(scoreboard, selectors.getStartTime());

        log.debug("[DEBUG] Parsed - status: {}, stadium: {}, startTime: {}", status, stadium, startTime);

        // 경기장 이름 정제
        stadium = cleanStadiumName(stadium, startTime);

        // 팀 정보 추출
        String awayName = safeTextCSS(scoreboard, selectors.getAwayTeam().getName());
        String homeName = safeTextCSS(scoreboard, selectors.getHomeTeam().getName());
        Integer awayScore = parseNullableInt(safeTextCSS(scoreboard, selectors.getAwayTeam().getScore()));
        Integer homeScore = parseNullableInt(safeTextCSS(scoreboard, selectors.getHomeTeam().getScore()));

        log.debug("[DEBUG] Parsed - awayName: {}, homeName: {}, awayScore: {}, homeScore: {}",
                awayName, homeName, awayScore, homeScore);

        // 박스스코어 URL
        ElementHandle boxScoreAnchor = queryCSS(scoreboard, selectors.getBoxScoreLink());
        String boxScoreUrl = boxScoreAnchor != null ? resolveUrl(boxScoreAnchor.getAttribute("href")) : null;

        // 테이블 점수 파싱
        ElementHandle table = queryCSS(scoreboard, selectors.getScoreTable().getTable());
        Map<String, KboScoreboardTeam> tableScores = parseTableScores(table);

        // 팀 데이터 병합
        KboScoreboardTeam awayTeam = mergeTeamData(awayName, awayScore, tableScores);
        KboScoreboardTeam homeTeam = mergeTeamData(homeName, homeScore, tableScores);

        if (awayTeam == null && homeTeam == null) {
            log.warn("스코어보드 파싱 실패: 팀 정보를 찾을 수 없습니다.");
            return Optional.empty();
        }

        // 투수 정보 파싱
        Pitcher pitcher = parsePitcher(scoreboard);

        return Optional.of(new KboScoreboardGame(
                date,
                emptyToNull(status),
                emptyToNull(stadium),
                parseLocalTimeEmptyToNull(startTime),
                emptyToNull(boxScoreUrl),
                awayTeam,
                homeTeam,
                awayScore,
                homeScore,
                pitcher.winning(),
                pitcher.saving(),
                pitcher.losing()
        ));
    }

    // ==================== 내부 파싱 메서드 ====================

    private String cleanStadiumName(String stadium, String startTime) {
        if (stadium != null && startTime != null) {
            return stadium.replace(startTime, "").trim();
        }
        return stadium != null ? stadium.trim() : null;
    }

    private Map<String, KboScoreboardTeam> parseTableScores(ElementHandle table) {
        Map<String, KboScoreboardTeam> scores = new LinkedHashMap<>();
        if (table == null) {
            return scores;
        }

        var tableSelectors = properties.getSelectors().getScoreboard().getScoreTable();
        List<ElementHandle> rows = table.querySelectorAll(tableSelectors.getRows());

        for (ElementHandle row : rows) {
            String teamName = safeTextCSS(row, tableSelectors.getTeamName());
            if (teamName == null || teamName.isBlank()) {
                continue;
            }

            List<ElementHandle> cells = row.querySelectorAll(tableSelectors.getCells());
            if (cells.isEmpty()) {
                continue;
            }

            int size = cells.size();
            int statsStart = Math.max(size - 4, 0);

            // 이닝별 점수
            List<String> inningScores = new ArrayList<>();
            for (int i = 0; i < statsStart; i++) {
                inningScores.add(normalizeScore(cells.get(i).innerText()));
            }

            // 통계 (R, H, E, B)
            Integer runs = statsStart < size ? parseNullableInt(cells.get(size - 4).innerText()) : null;
            Integer hits = statsStart < size ? parseNullableInt(cells.get(size - 3).innerText()) : null;
            Integer errors = statsStart < size ? parseNullableInt(cells.get(size - 2).innerText()) : null;
            Integer bases = statsStart < size ? parseNullableInt(cells.get(size - 1).innerText()) : null;

            scores.put(teamName.trim(), new KboScoreboardTeam(
                    teamName.trim(),
                    runs,
                    hits,
                    errors,
                    bases,
                    inningScores
            ));
        }

        return scores;
    }

    private KboScoreboardTeam mergeTeamData(
            String teamName,
            Integer displayScore,
            Map<String, KboScoreboardTeam> tableScores) {

        if (tableScores.isEmpty() && teamName == null) {
            return null;
        }

        // 팀 이름으로 테이블에서 찾기
        KboScoreboardTeam tableTeam = null;
        if (teamName != null) {
            tableTeam = tableScores.remove(teamName.trim());
        }

        // 못 찾으면 첫 번째 항목 사용
        if (tableTeam == null && !tableScores.isEmpty()) {
            String firstKey = tableScores.keySet().iterator().next();
            tableTeam = tableScores.remove(firstKey);
        }

        // 테이블 데이터 없으면 기본 정보만
        if (tableTeam == null) {
            if (teamName == null && displayScore == null) {
                return null;
            }
            return new KboScoreboardTeam(
                    teamName,
                    displayScore,
                    null,
                    null,
                    null,
                    List.of()
            );
        }

        // 병합
        Integer runs = tableTeam.runs() != null ? tableTeam.runs() : displayScore;
        return new KboScoreboardTeam(
                teamName != null ? teamName : tableTeam.name(),
                runs,
                tableTeam.hits(),
                tableTeam.errors(),
                tableTeam.basesOnBalls(),
                tableTeam.inningScores()
        );
    }

    private Pitcher parsePitcher(ElementHandle scoreboard) {
        var pitcherSelectors = properties.getSelectors().getScoreboard().getPitcher();

        String win = null, save = null, lose = null;
        try {
            ElementHandle container = scoreboard.querySelector(pitcherSelectors.getContainer());
            if (container != null) {
                List<ElementHandle> spans = container.querySelectorAll(pitcherSelectors.getSpans());
                for (ElementHandle s : spans) {
                    String raw = s.innerText();
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    raw = raw.replace('\u00A0', ' ').trim();
                    Matcher m = pitcherPattern.matcher(raw);
                    if (!m.find()) {
                        continue;
                    }

                    String label = m.group(1);
                    String name = m.group(2).trim();
                    if (name.isEmpty() || "-".equals(name)) {
                        continue;
                    }

                    switch (label) {
                        case "승" -> win = name;
                        case "세" -> save = name;
                        case "패" -> lose = name;
                    }
                }
            }
        } catch (PlaywrightException ignored) {
        }
        return new Pitcher(win, save, lose);
    }

    // ==================== 유틸리티 ====================

    private LocalTime parseLocalTimeEmptyToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : parseTime(t);
    }

    private LocalTime parseTime(String startTime) {
        try {
            return LocalTime.parse(startTime, timeFormatter);
        } catch (Exception e) {
            throw new GameSyncException("Invalid time format: " + startTime);
        }
    }

    private record Pitcher(String winning, String saving, String losing) {
    }
}
