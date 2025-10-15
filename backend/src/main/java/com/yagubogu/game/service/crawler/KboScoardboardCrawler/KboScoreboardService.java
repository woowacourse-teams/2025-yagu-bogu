package com.yagubogu.game.service.crawler.KboScoardboardCrawler;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.util.stream.Collectors.toMap;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.BatchResult;
import com.yagubogu.game.dto.FailedGame;
import com.yagubogu.game.dto.GameUpsertRow;
import com.yagubogu.game.dto.KboScoreboardGame;
import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.dto.UpsertResult;
import com.yagubogu.game.repository.GameJdbcBatchUpsertRepository;
import com.yagubogu.global.exception.BadRequestException;
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
import java.util.function.Function;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

@Slf4j
@RequiredArgsConstructor
@Service
public class KboScoreboardService {

    private final KboScoreboardCrawler kboScoreboardCrawler;
    private final KboScoreboardMapper mapper;
    private final GameJdbcBatchUpsertRepository batchUpsertRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate readOnlyTransactionTemplate;

    public List<ScoreboardResponse> fetchScoreboardRange(final LocalDate startDate, final LocalDate endDate) {
        StopWatch total = new StopWatch("scoreboardRange:" + startDate + "~" + endDate);
        total.start("crawl+persist");

        // 1) 크롤링 (트랜잭션 밖에서 실행)
        List<LocalDate> dates = getDatesBetweenInclusive(startDate, endDate);
        Map<LocalDate, List<KboScoreboardGame>> gamesByDate = kboScoreboardCrawler.crawlManyScoreboard(dates);

        // 2) 팀/구장 1회 로딩
        Map<String, Team> teamByShort = loadTeams();
        Map<String, Stadium> stadiumByLocation = loadStadiums();

        // 3) 더블헤더 순서 적용 + 평탄화
        List<KboScoreboardGame> allGames = new ArrayList<>();
        for (Map.Entry<LocalDate, List<KboScoreboardGame>> e : gamesByDate.entrySet()) {
            applyDoubleHeaderOrder(e.getValue());
            allGames.addAll(e.getValue());
        }

        // 4) 전체를 한 번에 "행"으로 변환
        List<GameUpsertRow> rows = convertToRows(
                allGames, teamByShort, stadiumByLocation);

        // 5) CHUNK별로 트랜잭션 분리하여 업서트
        UpsertResult upsertResult = upsertInChunks(rows);

        // 6) 실패한 경기 로그 출력
        if (!upsertResult.failedGames().isEmpty()) {
            log.error("[FAILED GAMES] 다음 경기들의 저장에 실패했습니다:");
            for (FailedGame failed : upsertResult.failedGames()) {
                log.error("  - {} vs {} ({})",
                        failed.awayTeamId(), failed.homeTeamId(), failed.date());
            }
        }

        // 7) 응답은 날짜별로 묶어 내려주기
        List<ScoreboardResponse> responses = new ArrayList<>();
        gamesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> responses.add(new ScoreboardResponse(e.getKey(), e.getValue())));

        total.stop();
        log.info("[SCOREBOARD_RANGE] {}~{} total={}ms success={} failed={}",
                startDate, endDate, total.getTotalTimeMillis(),
                upsertResult.successCount(), upsertResult.failedGames().size());
        return responses;
    }

    // 팀 로딩 (READ_ONLY 트랜잭션)
    private Map<String, Team> loadTeams() {
        return readOnlyTransactionTemplate.execute(status ->
                teamRepository.findAll().stream()
                        .collect(toMap(Team::getShortName, Function.identity()))
        );
    }

    // 구장 로딩 (READ_ONLY 트랜잭션)
    private Map<String, Stadium> loadStadiums() {
        return readOnlyTransactionTemplate.execute(status ->
                stadiumRepository.findAll().stream()
                        .collect(toMap(Stadium::getLocation, Function.identity()))
        );
    }

    // CHUNK별 트랜잭션 분리 업서트
    private UpsertResult upsertInChunks(List<GameUpsertRow> rows) {
        final int CHUNK = 1000;
        int totalSuccess = 0;
        List<FailedGame> failedGames = new ArrayList<>();

        for (int i = 0; i < rows.size(); i += CHUNK) {
            int from = i;
            int to = Math.min(i + CHUNK, rows.size());
            List<GameUpsertRow> chunk = rows.subList(from, to);

            // 각 CHUNK를 별도 트랜잭션으로 실행
            BatchResult result = transactionTemplate.execute(status -> {
                try {
                    return batchUpsertRepository.batchUpsert(chunk);
                } catch (Exception e) {
                    log.error("[CHUNK ERROR] chunk {}-{} 실패: {}", from, to, e.getMessage());
                    status.setRollbackOnly();
                    return new BatchResult(0,
                            IntStream.range(0, chunk.size()).boxed().toList(), 0);
                }
            });

            totalSuccess += result.success();

            // 실패한 인덱스를 실제 경기 정보로 변환
            if (result.hasFailures()) {
                for (Integer failedIndex : result.failedIndices()) {
                    int absoluteIndex = from + failedIndex;
                    if (absoluteIndex < rows.size()) {
                        GameUpsertRow failedRow = rows.get(absoluteIndex);
                        failedGames.add(new FailedGame(
                                failedRow.date(),
                                failedRow.gameCode(),
                                failedRow.awayTeamId(),
                                failedRow.homeTeamId()
                        ));
                    }
                }
                log.warn("[CHUNK FAILURE] chunk {}-{} 실패 인덱스: {}",
                        from, to, result.failedIndices());
            }

            log.info("[CHUNK COMMITTED] chunk {}-{} success={} failed={}",
                    from, to, result.success(), result.failedIndices().size());
        }

        return new UpsertResult(totalSuccess, failedGames);
    }

    private List<GameUpsertRow> convertToRows(
            List<KboScoreboardGame> allGames,
            Map<String, Team> teamByShort,
            Map<String, Stadium> stadiumByLocation) {

        List<GameUpsertRow> rows = new ArrayList<>(allGames.size());

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

            rows.add(new GameUpsertRow(
                    gameCode, stadium.getId(), home.getId(), away.getId(),
                    date, startAt, h.getRuns(), a.getRuns(),
                    dto.getWinningPitcher(), dto.getLosingPitcher(), state.name()
            ));
        }

        return rows;
    }

    private String generateGameCode(final LocalDate date, final Team homeTeam, final Team awayTeam,
                                    final int headerOrder) {
        return date.format(BASIC_ISO_DATE) + awayTeam.getTeamCode() + homeTeam.getTeamCode() + headerOrder;
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
            throw new BadRequestException("startDate와 endDate는 null일 수 없습니다.");
        }

        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("endDate는 startDate보다 이후여야 합니다.");
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
