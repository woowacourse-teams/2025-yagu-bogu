package com.yagubogu.game.service.crawler.KboScoardboardCrawler;

import static java.util.stream.Collectors.toMap;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboScoreboardGame;
import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.repository.GameJdbcBatchUpsertRepository;
import com.yagubogu.game.repository.GameJdbcBatchUpsertRepository.BatchResult;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class KboScoreboardService {

    private final KboScoreboardCrawler kboScoreboardCrawler;
    private final KboScoreboardMapper mapper;
    private final GameJdbcBatchUpsertRepository batchUpsertRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Transactional
    public List<ScoreboardResponse> fetchScoreboardRange(final LocalDate startDate, final LocalDate endDate) {
        StopWatch total = new StopWatch("scoreboardRange:" + startDate + "~" + endDate);
        total.start("crawl+persist");

        // 1) 크롤링(여러 날짜)
        List<LocalDate> dates = getDatesBetweenInclusive(startDate, endDate);
        Map<LocalDate, List<KboScoreboardGame>> gamesByDate = kboScoreboardCrawler.crawlManyScoreboard(dates);

        // 2) 팀/구장 1회 로딩(매핑 SELECT 제거)
        Map<String, Team> teamByShort = teamRepository.findAll().stream()
                .collect(toMap(Team::getShortName, Function.identity()));
        Map<String, Stadium> stadiumByLocation = stadiumRepository.findAll().stream()
                .collect(toMap(Stadium::getLocation, Function.identity()));

        // 3) 더블헤더 순서 적용 + 평탄화
        List<KboScoreboardGame> allGames = new ArrayList<>();
        for (Map.Entry<LocalDate, List<KboScoreboardGame>> e : gamesByDate.entrySet()) {
            applyDoubleHeaderOrder(e.getValue());
            allGames.addAll(e.getValue());
        }

        // 4) 전체를 한 번에 "행"으로 변환
        List<GameJdbcBatchUpsertRepository.GameUpsertRow> rows = new ArrayList<>(allGames.size());
        for (KboScoreboardGame dto : allGames) {
            Team away = teamByShort.get(dto.getAwayTeam().name());
            Team home = teamByShort.get(dto.getHomeTeam().name());
            Stadium stadium = stadiumByLocation.get(dto.getStadium());
            LocalDate date = dto.getDate();
            LocalTime startAt = parseStartAt(dto.getStartTime());

            ScoreBoard h = mapper.toScoreBoard(dto.getHomeTeam());
            ScoreBoard a = mapper.toScoreBoard(dto.getAwayTeam());
            GameState state = mapper.toState(dto.getStatus(), h.getRuns(), a.getRuns());

            String gameCode = generateGameCode(date, home, away, dto.getDoubleHeaderGameOrder());

            rows.add(new GameJdbcBatchUpsertRepository.GameUpsertRow(
                    gameCode, stadium.getId(), home.getId(), away.getId(),
                    date, startAt, h.getRuns(), a.getRuns(),
                    dto.getWinningPitcher(), dto.getLosingPitcher(), state.name()
            ));
        }

        // 5) 청크로 잘라 일괄 업서트
        final int CHUNK = 1000;
        int ok = 0;
        List<Integer> failures = new ArrayList<>();
        long begin = System.currentTimeMillis();
        for (int i = 0; i < rows.size(); i += CHUNK) {
            int to = Math.min(i + CHUNK, rows.size());
            var res = batchUpsertRepository.batchUpsert(rows.subList(i, to));
            ok += res.success();
            if (res.hasFailures()) failures.addAll(res.failedIndices());
        }
        long took = System.currentTimeMillis() - begin;
        log.info("[UPSERT-BATCH/ALL] period={}~{} rows={} success={} failed={} took={}ms",
                startDate, endDate, rows.size(), ok, failures.size(), took);
        if (!failures.isEmpty()) log.warn("failed indices (relative to chunks): {}", failures);

        // 6) 응답은 날짜별로 묶어 내려주고 싶으면 여기서만 그룹
        List<ScoreboardResponse> responses = new ArrayList<>();
        gamesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> responses.add(new ScoreboardResponse(e.getKey(), e.getValue())));

        total.stop();
        log.info("[SCOREBOARD_RANGE] {}~{} total={}ms", startDate, endDate, total.getTotalTimeMillis());
        return responses;
    }

    @Transactional
    public ScoreboardResponse fetchScoreboard(final LocalDate date) {
        List<KboScoreboardGame> games = kboScoreboardCrawler.crawlScoreboard(date);
        applyDoubleHeaderOrder(games);
        upsertAllWithJdbcBatch(games, date);

        return new ScoreboardResponse(date, games);
    }

    private void upsertAllWithJdbcBatch(List<KboScoreboardGame> list, LocalDate date) {
        if (list == null || list.isEmpty()) {
            return;
        }

        Map<String, Team> teamByShort = teamRepository.findAll().stream()
                .collect(toMap(Team::getShortName, Function.identity()));

        Map<String, Stadium> stadiumByLocation = stadiumRepository.findAll().stream()
                .collect(toMap(Stadium::getLocation, Function.identity()));

        List<GameJdbcBatchUpsertRepository.GameUpsertRow> rows = new ArrayList<>(list.size());
        for (KboScoreboardGame dto : list) {
            Team away = teamByShort.get(dto.getAwayTeam().name());
            Team home = teamByShort.get(dto.getHomeTeam().name());
            Stadium stadium = stadiumByLocation.get(dto.getStadium());

            LocalTime startAt = parseStartAt(dto.getStartTime());
            String gameCode = generateGameCode(date, home, away, dto.getDoubleHeaderGameOrder());

            ScoreBoard h = mapper.toScoreBoard(dto.getHomeTeam());
            ScoreBoard a = mapper.toScoreBoard(dto.getAwayTeam());
            GameState state = mapper.toState(dto.getStatus(), h.getRuns(), a.getRuns());

            rows.add(new GameJdbcBatchUpsertRepository.GameUpsertRow(
                    gameCode,
                    stadium.getId(),
                    home.getId(),
                    away.getId(),
                    date,
                    startAt,
                    h.getRuns(),
                    a.getRuns(),
                    dto.getWinningPitcher(),
                    dto.getLosingPitcher(),
                    state.name()
            ));
        }

        BatchResult batchResult = batchUpsertRepository.batchUpsert(rows);
        log.info("[UPSERT-BATCH] date={} size={} success={} failed={} took={}ms",
                date, rows.size(), batchResult.success(), batchResult.failedIndices().size(), batchResult.tookMs());
        if (batchResult.hasFailures()) {
            log.warn("[UPSERT-BATCH] failed indices: {}", batchResult.failedIndices());
        }
    }

    private String generateGameCode(final LocalDate date, final Team homeTeam, final Team awayTeam,
                                    final int headerOrder) {
        final String yyyymmdd = date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        return yyyymmdd + awayTeam.getTeamCode() + homeTeam.getTeamCode() + headerOrder;
    }

    private void applyDoubleHeaderOrder(final List<KboScoreboardGame> games) {
        Map<LocalDate, Map<String, List<KboScoreboardGame>>> grouped = new HashMap<>();

        for (KboScoreboardGame game : games) {
            grouped.computeIfAbsent(game.getDate(), ignored -> new HashMap<>())
                    .computeIfAbsent(game.getHomeTeam().name(), ignored -> new ArrayList<>())
                    .add(game);
        }

        Comparator<String> timeComparator = Comparator.nullsLast(Comparator.naturalOrder());

        for (Map<String, List<KboScoreboardGame>> byTeam : grouped.values()) {
            for (List<KboScoreboardGame> teamGames : byTeam.values()) {
                if (teamGames.size() <= 1) {
                    teamGames.forEach(g -> g.setDoubleHeaderGameOrder(0));
                    continue;
                }

                teamGames.sort(Comparator.comparing(KboScoreboardGame::getStartTime, timeComparator));
                for (int index = 0; index < teamGames.size(); index++) {
                    teamGames.get(index).setDoubleHeaderGameOrder(index);
                }
            }
        }
    }

    private LocalTime parseStartAt(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) {
            return LocalTime.of(0, 0);
        }
        try {
            return LocalTime.parse(hhmm.trim());
        } catch (Exception ignore) {
            String digits = hhmm.replaceAll("[^0-9]", " ").trim();
            String[] sp = digits.split("\\s+");
            int h = sp.length > 0 ? Integer.parseInt(sp[0]) : 0;
            int m = sp.length > 1 ? Integer.parseInt(sp[1]) : 0;
            return LocalTime.of(Math.min(23, h), Math.min(59, m));
        }
    }

    public static List<LocalDate> getDatesBetweenInclusive(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate와 endDate는 null일 수 없습니다.");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate는 startDate보다 이후여야 합니다.");
        }

        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }
}
