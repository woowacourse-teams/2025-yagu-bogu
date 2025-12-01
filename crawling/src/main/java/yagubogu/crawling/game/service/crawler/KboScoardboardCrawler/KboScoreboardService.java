package yagubogu.crawling.game.service.crawler.KboScoardboardCrawler;

import static java.util.stream.Collectors.toMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.event.GameFinalizedEvent;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.BronzeGameService;
import com.yagubogu.global.config.RabbitMQConfig;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;
import yagubogu.crawling.game.dto.BatchResult;
import yagubogu.crawling.game.dto.FailedGame;
import yagubogu.crawling.game.dto.GameUpsertRow;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.dto.ScoreboardResponse;
import yagubogu.crawling.game.dto.UpsertResult;
import yagubogu.crawling.game.repository.GameJdbcBatchUpsertRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class KboScoreboardService {

    private final KboScoreboardCrawler kboScoreboardCrawler;
    private final GameJdbcBatchUpsertRepository batchUpsertRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final TransactionTemplate transactionTemplate;
    private final TransactionTemplate readOnlyTransactionTemplate;
    private final GameRepository gameRepository;
    private final BronzeGameService bronzeGameService;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

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

        // 3) 평탄화
        List<KboScoreboardGame> allGames = gamesByDate.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 4) Bronze 레이어에 저장
        int savedCount = saveToBronzeLayerInChunks(allGames, teamByShort, stadiumByLocation);

        // 5) 응답은 날짜별로 묶어 내려주기
        List<ScoreboardResponse> responses = new ArrayList<>();
        gamesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> responses.add(new ScoreboardResponse(e.getKey(), e.getValue())));

        total.stop();
        log.info("[SCOREBOARD_RANGE] {}~{} total={}ms, Bronze saved={}",
                startDate, endDate, total.getTotalTimeMillis(), savedCount);
        return responses;
    }

    /**
     * 단일 날짜 조회만 (Adaptive Poller용)
     */
    public List<KboScoreboardGame> fetchScoreboardOnly(LocalDate date) {
        Map<LocalDate, List<KboScoreboardGame>> gamesByDate =
                kboScoreboardCrawler.crawl(List.of(date));

        return gamesByDate.getOrDefault(date, List.of());
    }

    /**
     * 스코어보드 데이터를 Bronze 레이어에 저장 (Adaptive Poller용)
     * 메달리온 아키텍처: 크롤러 → Bronze 저장 → ETL → Silver 변환
     */
    @Transactional
    public void updateFromScoreboard(String gameCode, KboScoreboardGame data) {
        try {
            // KboScoreboardGame을 JSON으로 직렬화하여 Bronze 저장
            String payload = objectMapper.writeValueAsString(data);
            LocalDate date = data.getDate();
            String stadium = data.getStadium();
            String homeTeamName = data.getHomeTeamScoreboard().name();
            String awayTeamName = data.getAwayTeamScoreboard().name();
            LocalTime startTime = data.getStartTime();
            bronzeGameService.upsertByNaturalKey(date, stadium, homeTeamName, awayTeamName, startTime, payload);
            log.debug("[BRONZE] Saved gameCode={}, state={}", gameCode, data.getStatus());

            // 경기 종료 시 RabbitMQ로 메시지 발행 (ETL 트리거)
            GameState state = GameState.fromName(data.getStatus());
            if (state == GameState.COMPLETED || state == GameState.CANCELED) {
                GameFinalizedEvent event = new GameFinalizedEvent(
                        date, stadium, homeTeamName, awayTeamName, startTime, state
                );
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.GAME_FINALIZED_EXCHANGE,
                        RabbitMQConfig.GAME_FINALIZED_ROUTING_KEY,
                        event
                );
                log.info("[RABBITMQ] Sent GameFinalizedEvent: gameCode={}, state={}", gameCode, state);
            }
        } catch (Exception e) {
            log.error("[BRONZE] Failed to save gameCode={}", gameCode, e);
            throw new GameSyncException("Failed to save Bronze layer: " + gameCode, e);
        }
    }

    /**
     * 배치로 크롤링한 데이터를 Bronze 레이어에 chunk 단위로 저장
     *
     * @return 실제로 저장된(변경 감지된) 경기 수
     */
    private int saveToBronzeLayerInChunks(List<KboScoreboardGame> allGames,
                                          Map<String, Team> teamByShort,
                                          Map<String, Stadium> stadiumByLocation) {
        final int CHUNK_SIZE = 100;
        int totalSaved = 0;

        for (int i = 0; i < allGames.size(); i += CHUNK_SIZE) {
            int to = Math.min(i + CHUNK_SIZE, allGames.size());
            List<KboScoreboardGame> chunk = allGames.subList(i, to);

            Integer chunkSaved = transactionTemplate.execute(status -> {
                int saved = 0;
                for (KboScoreboardGame game : chunk) {
                    try {
                        Team away = teamByShort.get(game.getAwayTeamScoreboard().name());
                        Team home = teamByShort.get(game.getHomeTeamScoreboard().name());
                        Stadium stadium = stadiumByLocation.get(game.getStadium());

                        if (away == null || home == null || stadium == null) {
                            log.warn("[BRONZE] Skip game due to missing reference: stadium={}, home={}, away={}",
                                    game.getStadium(), game.getHomeTeamScoreboard().name(),
                                    game.getAwayTeamScoreboard().name());
                            continue;
                        }

                        String payload = objectMapper.writeValueAsString(game);

                        boolean changed = bronzeGameService.upsertByNaturalKey(
                                game.getDate(),
                                game.getStadium(),
                                game.getHomeTeamScoreboard().name(),
                                game.getAwayTeamScoreboard().name(),
                                game.getStartTime(),
                                payload
                        );
                        if (changed) {
                            saved++;
                        }
                    } catch (Exception e) {
                        log.error("[BRONZE] Failed to save game: date={}, home={}, away={}",
                                game.getDate(), game.getHomeTeamScoreboard().name(),
                                game.getAwayTeamScoreboard().name(), e);
                    }
                }
                return saved;
            });

            totalSaved += (chunkSaved != null ? chunkSaved : 0);
        }

        log.info("[BRONZE_BATCH] Processed {} games, {} saved (changed)", allGames.size(), totalSaved);
        return totalSaved;
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

    /**
     * @deprecated 메달리온 아키텍처 도입으로 Bronze → ETL → Silver 흐름으로 변경됨
     * 이 메서드는 Silver 직접 업데이트 방식으로, 더 이상 사용되지 않음
     */
    @Deprecated
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
