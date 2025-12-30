package com.yagubogu.game.service;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yagubogu.game.domain.BronzeGame;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.PitcherResult;
import com.yagubogu.game.repository.BronzeGameRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Bronze → Silver ETL 서비스
 *
 * Bronze 레이어의 원본 JSON을 파싱하여 Silver 레이어(Game 테이블)로 변환
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GameEtlService {

    private final BronzeGameRepository bronzeGameRepository;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final ObjectMapper objectMapper;

    /**
     * 최근 수집된 Bronze 데이터를 Silver로 ETL
     * ETL 중에 날짜별로 더블헤더 순서를 계산하여 올바른 gameCode로 저장
     *
     * @param since 이 시각 이후 수집된 데이터만 처리
     * @return 처리된 게임 수
     */
    @Transactional
    public int transformBronzeToSilver(final LocalDateTime since) {
         final List<BronzeGame> bronzeGames = bronzeGameRepository.findPendingEtl(since);

        if (bronzeGames.isEmpty()) {
            log.debug("No bronze data to transform since {}", since);
            return 0;
        }

        // 날짜별로 그룹화
        final Map<LocalDate, List<BronzeGame>> byDate = new HashMap<>();
        for (BronzeGame bg : bronzeGames) {
            byDate.computeIfAbsent(bg.getDate(), k -> new ArrayList<>()).add(bg);
        }

        int transformedCount = 0;
        for (Map.Entry<LocalDate, List<BronzeGame>> entry : byDate.entrySet()) {
            final List<BronzeGame> gamesOnDate = entry.getValue();

            // 1. 같은 날짜의 경기들을 홈팀별로 그룹화하고 시작 시간 순으로 정렬하여 더블헤더 순서 계산
            final Map<String, List<BronzeGame>> byHomeTeam = new HashMap<>();
            for (BronzeGame bg : gamesOnDate) {
                byHomeTeam.computeIfAbsent(bg.getHomeTeam(), k -> new ArrayList<>()).add(bg);
            }

            // 2. 각 홈팀의 경기를 시작 시간 순으로 정렬
            final Comparator<LocalTime> timeComparator = Comparator.nullsLast(Comparator.naturalOrder());
            final Map<BronzeGame, Integer> doubleHeaderOrderMap = new HashMap<>();

            for (List<BronzeGame> homeTeamGames : byHomeTeam.values()) {
                homeTeamGames.sort(Comparator.comparing(BronzeGame::getStartTime, timeComparator));

                // 순서 할당
                for (int order = 0; order < homeTeamGames.size(); order++) {
                    doubleHeaderOrderMap.put(homeTeamGames.get(order), order);
                }
            }

            // 3. 각 게임을 올바른 더블헤더 순서와 함께 변환
            for (BronzeGame bronzeGame : gamesOnDate) {
                try {
                    final int order = doubleHeaderOrderMap.getOrDefault(bronzeGame, 0);
                    transformSingleGame(bronzeGame, order);
                    bronzeGame.markEtlProcessed(LocalDateTime.now());

                    transformedCount++;
                } catch (Exception e) {
                    log.error("Failed to transform bronze game: bronzeGameId={}",
                            bronzeGame.getId(), e);
                }
            }
        }

        log.info("ETL completed: {} games transformed", transformedCount);
        return transformedCount;
    }

    /**
     * 특정 게임 코드들에 대해 즉시 ETL 실행
     *
     * 경기 종료 이벤트 발생 시 호출
     */
    @Transactional
    public void transformSpecificGame(
            final LocalDate date,
            final String stadium,
            final String homeTeam,
            final String awayTeam,
            final LocalTime startTime
    ) {
        final Optional<BronzeGame> bronzeGameOpt = bronzeGameRepository
                .findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartTime(
                        date, stadium, homeTeam, awayTeam, startTime
                );

        if (bronzeGameOpt.isEmpty()) {
            log.warn("Bronze game not found: date={}, stadium={}, home={}, away={}",
                    date, stadium, homeTeam, awayTeam);
            return;
        }

        try {
            // 해당 날짜의 같은 홈팀의 모든 경기를 조회하여 더블헤더 순서 계산
            final List<BronzeGame> allGamesOnDate = bronzeGameRepository.findAllCollectedSince(
                            date.atStartOfDay()
                    ).stream()
                    .filter(bg -> bg.getDate().equals(date) && bg.getHomeTeam().equals(homeTeam))
                    .sorted(Comparator.comparing(BronzeGame::getStartTime,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

            // 현재 경기의 순서 찾기
            final BronzeGame targetGame = bronzeGameOpt.get();
            int order = 0;
            for (int i = 0; i < allGamesOnDate.size(); i++) {
                if (allGamesOnDate.get(i).getId().equals(targetGame.getId())) {
                    order = i;
                    break;
                }
            }

            transformSingleGame(targetGame, order);
            log.info("Immediate ETL completed: date={}, home={}, away={}, order={}",
                    date, homeTeam, awayTeam, order);
        } catch (Exception e) {
            log.error("Failed to transform game: date={}, home={}, away={}",
                    date, homeTeam, awayTeam, e);
        }
    }

    /**
     * 더블헤더 순서 적용
     * 같은 날짜, 같은 홈팀의 경기를 시작 시간 순으로 정렬하여 게임 코드에 순서 반영
     */
    @Transactional
    public void applyDoubleHeaderOrder(final LocalDate date) {
        final List<Game> games = gameRepository.findAllByDate(date);

        if (games.isEmpty()) {
            return;
        }

        // 날짜별, 홈팀별로 그룹화
        final Map<String, List<Game>> groupedByHomeTeam = new HashMap<>();
        for (Game game : games) {
            final String homeTeamShortName = game.getHomeTeam().getShortName();
            groupedByHomeTeam.computeIfAbsent(homeTeamShortName, k -> new ArrayList<>()).add(game);
        }

        // 시작 시간 순으로 정렬하고 게임 코드 재생성
        final Comparator<LocalTime> timeComparator = Comparator.nullsLast(Comparator.naturalOrder());

        for (List<Game> homeTeamGames : groupedByHomeTeam.values()) {
            if (homeTeamGames.size() <= 1) {
                // 더블헤더가 아니면 order=0
                continue;
            }

            // 시작 시간 순으로 정렬
            homeTeamGames.sort(Comparator.comparing(Game::getStartAt, timeComparator));

            // 순서대로 게임 코드 재생성
            for (int index = 0; index < homeTeamGames.size(); index++) {
                final Game game = homeTeamGames.get(index);
                final String newGameCode = generateGameCode(
                        game.getDate(),
                        game.getHomeTeam(),
                        game.getAwayTeam(),
                        index
                );

                // 게임 코드가 변경된 경우에만 업데이트
                if (!game.getGameCode().equals(newGameCode)) {
                    game.update(
                            game.getStadium(),
                            game.getHomeTeam(),
                            game.getAwayTeam(),
                            game.getDate(),
                            game.getStartAt(),
                            newGameCode,
                            game.getHomeScore(),
                            game.getAwayScore(),
                            game.getHomeScoreBoard(),
                            game.getAwayScoreBoard(),
                            game.getHomePitcher(),
                            game.getAwayPitcher(),
                            game.getGameState()
                    );

                    log.info("[DOUBLE_HEADER] Updated gameCode: old={}, new={}",
                            game.getGameCode(), newGameCode);
                }
            }
        }
    }

    /**
     * 단일 BronzeGame을 Silver(Game)로 변환
     * gameCode는 해당 게임의 더블헤더 순서를 미리 계산하여 생성
     */
    private void transformSingleGame(final BronzeGame bronzeGame, final int doubleHeaderOrder) throws Exception {
        // 1. Extract: Bronze에서 JSON 파싱
        final JsonNode json = objectMapper.readTree(bronzeGame.getPayload());

        // JSON 필드 추출
        final String status = json.path("status").asText(null);
        final String stadiumLocation = json.path("stadium").asText();
        final LocalDate date = LocalDate.parse(json.path("date").asText());
        final String startTimeStr = json.path("startTime").asText(null);
        final LocalTime startTime = startTimeStr != null && !startTimeStr.isEmpty()
                ? LocalTime.parse(startTimeStr)
                : LocalTime.of(0, 0);

        final Integer homeScore = json.path("homeScore").isNull() ? null : json.path("homeScore").asInt();
        final Integer awayScore = json.path("awayScore").isNull() ? null : json.path("awayScore").asInt();
        final String winningPitcher = json.path("winningPitcher").asText(null);
        final String losingPitcher = json.path("losingPitcher").asText(null);

        final JsonNode homeTeamScoreboard = json.path("homeTeamScoreboard");
        final JsonNode awayTeamScoreboard = json.path("awayTeamScoreboard");

        final String homeTeamName = homeTeamScoreboard.path("name").asText();
        final String awayTeamName = awayTeamScoreboard.path("name").asText();

        // 2. Transform: Team/Stadium 조회
        final Team homeTeam = teamRepository.findByShortName(homeTeamName)
                .orElseThrow(() -> new NotFoundException("HomeTeam not found: " + homeTeamName));
        final Team awayTeam = teamRepository.findByShortName(awayTeamName)
                .orElseThrow(() -> new NotFoundException("AwayTeam not found: " + awayTeamName));
        final Stadium stadium = stadiumRepository.findByLocation(stadiumLocation)
                .orElseThrow(() -> new NotFoundException("Stadium not found: " + stadiumLocation));

        // 올바른 더블헤더 순서로 게임 코드 생성
        final String gameCode = generateGameCode(date, homeTeam, awayTeam, doubleHeaderOrder);

        final GameState gameState = GameState.fromName(status);

        PitcherResult result = assignPitchers(homeScore, awayScore, winningPitcher, losingPitcher);
        final String homePitcher = result.home();
        final String awayPitcher = result.away();

        final ScoreBoard homeScoreBoard = convertToScoreBoardFromJson(homeTeamScoreboard);
        final ScoreBoard awayScoreBoard = convertToScoreBoardFromJson(awayTeamScoreboard);

        // 3. Load: Game 엔티티 UPSERT
        // Natural Key로 조회하여 더블헤더 구분
        final Optional<Game> existingGameOpt = gameRepository.findByDateAndStadiumAndHomeTeamAndAwayTeamAndStartAt(
                date, stadium, homeTeam, awayTeam, startTime
        );

        existingGameOpt.ifPresentOrElse(
                existingGame -> {
                    Game updated = updateExistingGame(
                            existingGame,
                            stadium, homeTeam, awayTeam,
                            date, startTime, gameCode,
                            homeScore, awayScore,
                            homeScoreBoard, awayScoreBoard,
                            homePitcher, awayPitcher, gameState
                    );
                    log.debug("[ETL] Updated Game: stadium={}, date={}, startTime={}, gameCode={}",
                            stadium, date, startTime, gameCode);
                },
                () -> {
                    Game newGame = createNewGame(
                            stadium, homeTeam, awayTeam,
                            date, startTime, gameCode,
                            homeScore, awayScore,
                            homeScoreBoard, awayScoreBoard,
                            homePitcher, awayPitcher, gameState
                    );
                    gameRepository.save(newGame);
                    log.debug("[ETL] Created Game: stadium={}, date={}, startTime={}, gameCode={}",
                            stadium, date, startTime, gameCode);
                }
        );

        log.debug("[ETL] Processed Game: gameCode={}, state={}", gameCode, gameState);
    }

    /**
     * JSON → ScoreBoard 변환
     */
    private ScoreBoard convertToScoreBoardFromJson(final JsonNode teamNode) {
        if (teamNode == null || teamNode.isMissingNode()) {
            return new ScoreBoard(0, 0, 0, 0, new ArrayList<>());
        }

        final int runs = teamNode.path("runs").isNull() ? 0 : teamNode.path("runs").asInt(0);
        final int hits = teamNode.path("hits").isNull() ? 0 : teamNode.path("hits").asInt(0);
        final int errors = teamNode.path("errors").isNull() ? 0 : teamNode.path("errors").asInt(0);
        final int basesOnBalls = teamNode.path("basesOnBalls").isNull() ? 0 : teamNode.path("basesOnBalls").asInt(0);

        final List<String> inningScores = new ArrayList<>();
        final JsonNode inningScoresNode = teamNode.path("inningScores");
        if (inningScoresNode.isArray()) {
            for (JsonNode inningScore : inningScoresNode) {
                inningScores.add(inningScore.asText());
            }
        }

        return new ScoreBoard(runs, hits, errors, basesOnBalls, inningScores);
    }

    /**
     * 게임 코드 생성: YYYYMMDD + 원정팀코드 + 홈팀코드 + 더블헤더순서
     */
    private String generateGameCode(final LocalDate date, final Team homeTeam, final Team awayTeam,
                                    final int headerOrder) {
        return date.format(BASIC_ISO_DATE) + awayTeam.getTeamCode() + homeTeam.getTeamCode() + headerOrder;
    }

    private static PitcherResult assignPitchers(Integer homeScore, Integer awayScore, String winningPitcher,
                                                String losingPitcher
    ) {
        if (homeScore == null || awayScore == null) {
            return new PitcherResult(null, null);
        }

        if (homeScore > awayScore) {
            return new PitcherResult(winningPitcher, losingPitcher);
        }

        if (homeScore < awayScore) {
            return new PitcherResult(losingPitcher, winningPitcher);
        }

        return new PitcherResult(null, null);
    }

    private ScoreBoard resolveScoreBoard(
            ScoreBoard existing,
            ScoreBoard incoming
    ) {
        if (existing == null) {
            return incoming;
        }

        existing.update(
                incoming.getRuns(),
                incoming.getHits(),
                incoming.getErrors(),
                incoming.getBasesOnBalls(),
                incoming.getInningScores()
        );

        return existing;
    }

    private Game updateExistingGame(
            Game existing,
            Stadium stadium, Team homeTeam, Team awayTeam,
            LocalDate date, LocalTime startTime, String gameCode,
            Integer homeScore, Integer awayScore,
            ScoreBoard homeScoreBoard, ScoreBoard awayScoreBoard,
            String homePitcher, String awayPitcher,
            GameState gameState
    ) {
        ScoreBoard updatedHome = resolveScoreBoard(existing.getHomeScoreBoard(), homeScoreBoard);
        ScoreBoard updatedAway = resolveScoreBoard(existing.getAwayScoreBoard(), awayScoreBoard);

        existing.update(
                stadium, homeTeam, awayTeam,
                date, startTime, gameCode,
                homeScore, awayScore,
                updatedHome, updatedAway,
                homePitcher, awayPitcher,
                gameState
        );
        return existing;
    }

    private Game createNewGame(
            Stadium stadium, Team homeTeam, Team awayTeam,
            LocalDate date, LocalTime startTime, String gameCode,
            Integer homeScore, Integer awayScore,
            ScoreBoard homeScoreBoard, ScoreBoard awayScoreBoard,
            String homePitcher, String awayPitcher,
            GameState gameState
    ) {
        return new Game(
                stadium, homeTeam, awayTeam,
                date, startTime, gameCode,
                homeScore, awayScore,
                homeScoreBoard, awayScoreBoard,
                homePitcher, awayPitcher,
                gameState
        );
    }
}
