package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;
import static java.util.stream.Collectors.toMap;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;
import yagubogu.crawling.game.dto.BatchResult;
import yagubogu.crawling.game.dto.FailedGame;
import yagubogu.crawling.game.dto.GameUpsertRow;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.KboScoreboardTeam;
import yagubogu.crawling.game.dto.PitcherAssignment;
import yagubogu.crawling.game.dto.ScoreboardResponse;
import yagubogu.crawling.game.dto.UpsertResult;
import yagubogu.crawling.game.repository.GameJdbcBatchUpsertRepository;

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
    private final GameRepository gameRepository;

    private Map<String, Stadium> stadiumCache = new ConcurrentHashMap<>();
    private Map<String, Team> teamCache = new ConcurrentHashMap<>();

    /**
     * 애플리케이션 시작 시 캐시 로딩
     */
    @PostConstruct
    public void initCache() {
        reloadCache();
    }

    public List<ScoreboardResponse> fetchScoreboardRange(final LocalDate startDate, final LocalDate endDate) {
        StopWatch total = new StopWatch("scoreboardRange:" + startDate + "~" + endDate);
        total.start("crawl + persist");

        // 1) 크롤링 (트랜잭션 밖에서 실행)
        List<LocalDate> dates = getDatesBetweenInclusive(startDate, endDate);
        Map<LocalDate, List<KboScoreboardGame>> gamesByDate = kboScoreboardCrawler.crawl(dates);

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
        List<GameUpsertRow> rows = convertToRows(allGames, teamByShort, stadiumByLocation);

        // 5) CHUNK별로 트랜잭션 분리하여 업서트
        UpsertResult upsertResult = upsertInChunks(rows);

        // 6) 실패한 경기 로그 출력
        if (!upsertResult.failedGames().isEmpty()) {
            log.error("[FAILED GAMES] 다음 경기들의 저장에 실패했습니다:");
            for (FailedGame failed : upsertResult.failedGames()) {
                log.error("  - {} vs {} ({})", failed.awayTeamId(), failed.homeTeamId(), failed.date());
            }
        }

        // 7) 응답은 날짜별로 묶어 내려주기
        List<ScoreboardResponse> responses = new ArrayList<>();
        gamesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> responses.add(new ScoreboardResponse(e.getKey(), e.getValue())));

        total.stop();
        log.info("[SCOREBOARD_RANGE] {}~{} total={}ms success={} failed={}",
                startDate, endDate, total.getTotalTimeMillis(), upsertResult.successCount(),
                upsertResult.failedGames().size());
        return responses;
    }

    /**
     * 단일 날짜 조회만 (Adaptive Poller용)
     */
    public List<KboScoreboardGame> fetchScoreboardOnly(LocalDate date) {
        Map<LocalDate, List<KboScoreboardGame>> gamesByDate =
                kboScoreboardCrawler.crawl(List.of(date));

        List<KboScoreboardGame> games = gamesByDate.getOrDefault(date, List.of());
        applyDoubleHeaderOrder(games);

        return games;
    }

    /**
     * 스코어보드 데이터로 업데이트 (Adaptive Poller용)
     * JPA 더티체킹으로 변경된 필드만 UPDATE
     */
    @Transactional
    public void updateFromScoreboard(String gameCode, KboScoreboardGame data) {
        Game game = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameCode));

        String location = data.getStadium();
        Stadium stadium = stadiumCache.get(location);
        if (stadium == null) {
            log.warn("Stadium not found in cache: {}, skipping update", data.getStadium());
            return;
        }

        String homeTeamName = data.getHomeTeamScoreboard().name();
        Team homeTeam = teamCache.get(homeTeamName);
        if (homeTeam == null) {
            log.warn("HomeTeam not found in cache: {}, skipping update", homeTeamName);
            return;
        }

        String awayTeamName = data.getAwayTeamScoreboard().name();
        Team awayTeam = teamCache.get(awayTeamName);
        if (awayTeam == null) {
            log.warn("AwayTeam not found in cache: {}, skipping update", awayTeamName);
            return;
        }

        Integer homeScore = data.getHomeScore();
        Integer awayScore = data.getAwayScore();
        String winningPitcher = data.getWinningPitcher();
        String losingPitcher = data.getLosingPitcher();
        PitcherAssignment pitcherAssignment = assignPitchersByScore(homeScore, awayScore, winningPitcher,
                losingPitcher);

        KboScoreboardTeam kboHomeTeamScoreboard = data.getHomeTeamScoreboard();
        KboScoreboardTeam kboAwayTeamScoreboard = data.getAwayTeamScoreboard();

        ScoreBoard homeScoreBoard = updateOrCreateScoreBoard(
                game.getHomeScoreBoard(),
                kboHomeTeamScoreboard
        );
        ScoreBoard awayScoreBoard = updateOrCreateScoreBoard(
                game.getAwayScoreBoard(),
                kboAwayTeamScoreboard
        );

        LocalDate date = data.getDate();
        game.update(stadium, homeTeam, awayTeam, date, data.getStartTime(), gameCode,
                homeScore, awayScore, homeScoreBoard, awayScoreBoard,
                pitcherAssignment.homePitcher(), pitcherAssignment.awayPitcher(), GameState.fromName(data.getStatus())
        );
    }

    private ScoreBoard updateOrCreateScoreBoard(ScoreBoard existing, KboScoreboardTeam data) {
        if (existing == null) {
            return new ScoreBoard(
                    data.runs(),
                    data.hits(),
                    data.errors(),
                    data.basesOnBalls(),
                    data.inningScores()
            );
        }

        existing.update(
                data.runs(),
                data.hits(),
                data.errors(),
                data.basesOnBalls(),
                data.inningScores()
        );
        return existing;
    }

    /**
     * 캐시 갱신 (팀/구장 변경 시 호출)
     */
    private void reloadCache() {
        this.stadiumCache = stadiumRepository.findAll().stream()
                .collect(Collectors.toConcurrentMap(Stadium::getLocation, s -> s));

        this.teamCache = teamRepository.findAll().stream()
                .collect(Collectors.toConcurrentMap(Team::getShortName, t -> t));

        log.info("[CACHE] Reloaded {} stadiums, {} teams", stadiumCache.size(), teamCache.size());
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
                    status.setRollbackOnly();
                    return new BatchResult(0, IntStream.range(0, chunk.size()).boxed().toList(), 0);
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
            }
        }

        return new UpsertResult(totalSuccess, failedGames);
    }

    private List<GameUpsertRow> convertToRows(
            List<KboScoreboardGame> allGames,
            Map<String, Team> teamByShort,
            Map<String, Stadium> stadiumByLocation
    ) {

        List<GameUpsertRow> rows = new ArrayList<>(allGames.size());

        for (KboScoreboardGame dto : allGames) {
            Team away = teamByShort.get(dto.getAwayTeamScoreboard().name());
            Team home = teamByShort.get(dto.getHomeTeamScoreboard().name());
            Stadium stadium = stadiumByLocation.get(dto.getStadium());
            LocalDate date = dto.getDate();
            LocalTime startAt = dto.getStartTime();

            ScoreBoard h = mapper.toScoreBoard(dto.getHomeTeamScoreboard());
            ScoreBoard a = mapper.toScoreBoard(dto.getAwayTeamScoreboard());
            GameState state = mapper.toState(dto.getStatus(), h.getRuns(), a.getRuns());

            String gameCode = generateGameCode(date, home, away, dto.getDoubleHeaderGameOrder());

            String winningPitcher = dto.getWinningPitcher();
            String losingPitcher = dto.getLosingPitcher();
            Integer homeScore = dto.getHomeScore();
            Integer awayScore = dto.getAwayScore();
            PitcherAssignment pitcherAssignment = assignPitchersByScore(homeScore, awayScore, winningPitcher,
                    losingPitcher);

            rows.add(new GameUpsertRow(
                    gameCode, stadium.getId(), home.getId(), away.getId(),
                    date, startAt, h.getRuns(), a.getRuns(),
                    pitcherAssignment.homePitcher(), pitcherAssignment.awayPitcher(), state.name()
            ));
        }

        return rows;
    }

    private PitcherAssignment assignPitchersByScore(
            Integer homeScore,
            Integer awayScore,
            String winningPitcher,
            String losingPitcher
    ) {
        if (homeScore == null || awayScore == null) {
            return new PitcherAssignment(null, null);
        }

        if (homeScore > awayScore) {
            return new PitcherAssignment(winningPitcher, losingPitcher);
        } else {
            return new PitcherAssignment(losingPitcher, winningPitcher);
        }
    }

    private String generateGameCode(final LocalDate date, final Team homeTeam, final Team awayTeam,
                                    final int headerOrder) {
        return date.format(BASIC_ISO_DATE) + awayTeam.getTeamCode() + homeTeam.getTeamCode() + headerOrder;
    }

    private void applyDoubleHeaderOrder(final List<KboScoreboardGame> games) {
        Map<LocalDate, Map<String, List<KboScoreboardGame>>> grouped = new HashMap<>();

        for (KboScoreboardGame game : games) {
            grouped.computeIfAbsent(game.getDate(), ignored -> new HashMap<>())
                    .computeIfAbsent(game.getHomeTeamScoreboard().name(), ignored -> new ArrayList<>())
                    .add(game);
        }

        Comparator<LocalTime> timeComparator = Comparator.nullsLast(Comparator.naturalOrder());

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

    private List<LocalDate> getDatesBetweenInclusive(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new GameSyncException("startDate와 endDate는 null일 수 없습니다.");
        }

        if (endDate.isBefore(startDate)) {
            throw new GameSyncException("endDate는 startDate보다 이후여야 합니다.");
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
