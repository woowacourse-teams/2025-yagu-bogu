package com.yagubogu.game.service.crawler.KboScoardboardCrawler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboScoreboardGame;
import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private final GameRepository gameRepository;
    private final KboScoreboardMapper mapper;

    @Transactional
    public List<ScoreboardResponse> fetchScoreboardRange(final LocalDate startDate, final LocalDate endDate) {
        StopWatch total = new StopWatch("scoreboardRange:" + startDate + "~" + endDate);
        total.start("scoreboard");
        List<ScoreboardResponse> responses = new ArrayList<>();
        StopWatch sw = new StopWatch("scoreboard:" + startDate);

        List<LocalDate> dates = getDatesBetweenInclusive(startDate, endDate);
        sw.start("crawl");
        Map<LocalDate, List<KboScoreboardGame>> gamesByDate = kboScoreboardCrawler.crawlManyScoreboard(dates);
        sw.stop();

        for (Entry<LocalDate, List<KboScoreboardGame>> entry : gamesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<KboScoreboardGame> games = entry.getValue();

            sw.start("doubleHeaderOrder");
            applyDoubleHeaderOrder(games);
            sw.stop();

            sw.start("upsertAll");
            upsertAll(games, date);
            sw.stop();

            log.info("[SCOREBOARD] date={} phases={} total={}ms", date, sw.prettyPrint(), sw.getTotalTimeMillis());
            ScoreboardResponse response = new ScoreboardResponse(date, games);
            responses.add(response);
        }

        total.stop();
        log.info("[SCOREBOARD_RANGE] {}~{} total={}ms", startDate, endDate, total.getTotalTimeMillis());
        return responses;
    }

    @Transactional
    public ScoreboardResponse fetchScoreboard(final LocalDate date) {
        StopWatch sw = new StopWatch("scoreboard:" + date);
        sw.start("crawl");
        List<KboScoreboardGame> games = kboScoreboardCrawler.crawlScoreboard(date);
        sw.stop();

        sw.start("doubleHeaderOrder");
        applyDoubleHeaderOrder(games);
        sw.stop();

        sw.start("upsertAll");
        upsertAll(games, date);
        sw.stop();

        log.info("[SCOREBOARD] date={} phases={} total={}ms", date, sw.prettyPrint(), sw.getTotalTimeMillis());
        return new ScoreboardResponse(date, games);
    }

    private List<Game> upsertAll(final List<KboScoreboardGame> list, final LocalDate date) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }

        StopWatch sw = new StopWatch("upsertAll:" + date);
        List<Game> saved = new ArrayList<>(list.size());

        sw.start("persist");
        for (KboScoreboardGame dto : list) {
            saved.add(upsertOne(dto, date));
        }
        sw.stop();

        log.info("[UPSERT] date={} phases={} total={}ms size={}", date, sw.prettyPrint(), sw.getTotalTimeMillis(),
                list.size());
        return saved;
    }

//    private Game upsertOne(KboScoreboardGame dto, final LocalDate date) {
//        final String gameKeyForLog = dto.getDate() + "|" + dto.getHomeTeam().name() + " vs " + dto.getAwayTeam().name();
//
//        StopWatch sw = new StopWatch("upsertOne:" + date);
//        Game result;
//
//        // 1) 매핑
//        sw.start("mapping");
//        Team awayTeam = mapper.resolveTeamFromShortName(dto.getAwayTeam().name());
//        Team homeTeam = mapper.resolveTeamFromShortName(dto.getHomeTeam().name());
//        Stadium stadium = mapper.resolveStadium(dto.getStadium());
//        LocalTime startAt = parseStartAt(dto.getStartTime());
//
//        ScoreBoard awaySb = mapper.toScoreBoard(dto.getAwayTeam());
//        ScoreBoard homeSb = mapper.toScoreBoard(dto.getHomeTeam());
//        GameState state = mapper.toState(dto.getStatus(), homeSb.getRuns(), awaySb.getRuns());
//        int headerOrder = dto.getDoubleHeaderGameOrder();
//        sw.stop(); // mapping
//
//        // 2) 조회 (자연키 or gameCode)
//        sw.start("repo.find");
//        Optional<Game> found = gameRepository.findByNaturalKey(
//                date, homeTeam.getTeamCode(), awayTeam.getTeamCode(), startAt
//        );
//        sw.stop(); // repo.find
//
//        if (found.isPresent()) {
//            Game existing = found.get();
//
//            // 3-a) 메모리 내 갱신
//            sw.start("update.fields");
//            existing.updateSchedule(stadium, homeTeam, awayTeam, date, startAt, state);
//            if (state != GameState.SCHEDULED) {
//                existing.updateScoreBoard(homeSb, awaySb, dto.getWinningPitcher(), dto.getLosingPitcher());
//            }
//            existing.updateGameState(state);
//            sw.stop(); // update.fields
//
//            // 4-a) 저장 (save는 merge가 아니라 dirty checking으로 넘어갈 수 있으니 flush로 실제 시간 측정)
//            sw.start("repo.save(existing)");
//            // save 호출이 불필요해도 호출해 둬도 무해(JPA 구현에 따라 no-op일 수 있음)
//            gameRepository.save(existing);
//            sw.stop(); // repo.save(existing)
//
//            sw.start("repo.flush(existing)");
//            // 실제 DB 왕복/쓰기 시간 강제 측정
//            gameRepository.flush();
//            sw.stop(); // repo.flush(existing)
//
//            result = existing;
//        } else {
//            // 3-b) 신규 생성
//            sw.start("create.entity");
//            String gameCode = generateGameCode(date, homeTeam, awayTeam, headerOrder);
//            Game created = new Game(
//                    stadium,
//                    homeTeam,
//                    awayTeam,
//                    date,
//                    startAt,
//                    gameCode,
//                    homeSb.getRuns(),
//                    awaySb.getRuns(),
//                    homeSb,
//                    awaySb,
//                    dto.getWinningPitcher(),
//                    dto.getLosingPitcher(),
//                    state
//            );
//            sw.stop(); // create.entity
//
//            // 4-b) 저장/flush
//            sw.start("repo.save(new)");
//            Game saved = gameRepository.save(created);
//            sw.stop(); // repo.save(new)
//
//            sw.start("repo.flush(new)");
//            gameRepository.flush();
//            sw.stop(); // repo.flush(new)
//
//            result = saved;
//        }
//
//        // 최종 로그
//        log.info("[UPSERT_ONE] key={} status={} total={}ms phases={}",
//                gameKeyForLog,
//                (found.isPresent() ? "UPDATE" : "CREATE"),
//                sw.getTotalTimeMillis(),
//                sw.prettyPrint());
//
//        return result;
//    }

    private Game upsertOne(KboScoreboardGame dto, final LocalDate date) {
        StopWatch sw = new StopWatch("upsertOne:" + date);
        // 1) 매핑
        sw.start("mapping");
        Team awayTeam = mapper.resolveTeamFromShortName(dto.getAwayTeam().name());
        Team homeTeam = mapper.resolveTeamFromShortName(dto.getHomeTeam().name());
        log.info("날짜: {}, {} vs {}", date, awayTeam.getName(), homeTeam.getName());

        Stadium stadium = mapper.resolveStadium(dto.getStadium());
        LocalTime startAt = parseStartAt(dto.getStartTime());

        ScoreBoard awaySb = mapper.toScoreBoard(dto.getAwayTeam());
        ScoreBoard homeSb = mapper.toScoreBoard(dto.getHomeTeam());
        GameState state = mapper.toState(dto.getStatus(), homeSb.getRuns(), awaySb.getRuns());
        int headerOrder = dto.getDoubleHeaderGameOrder();
        sw.stop();

        // 2) upsert by gameCode
        Game game = gameRepository.findByNaturalKey(date, homeTeam.getTeamCode(), awayTeam.getTeamCode(), startAt)
                .map(existing -> {
                    sw.start("upsert when existing");
                    // 스케줄 정보 갱신(시간/구장 바뀔 수 있음)
                    existing.updateSchedule(stadium, homeTeam, awayTeam, date, startAt, state);

                    // 스코어/투수 갱신(경기 전이면 점수보드가 0일 수 있으므로 상태 보고 반영)
                    if (state != GameState.SCHEDULED) {
                        existing.updateScoreBoard(homeSb, awaySb, dto.getWinningPitcher(), dto.getLosingPitcher());
                    }
                    // 상태 최종 보정
                    existing.updateGameState(state);
                    sw.stop();
                    return existing;
                })
                .orElseGet(() -> {
                    // 신규 생성
                    sw.start("upsert when create");
                    String gameCode = generateGameCode(date, homeTeam, awayTeam, headerOrder);
                    sw.stop();
                    return gameRepository.save(new Game(
                            stadium,
                            homeTeam,
                            awayTeam,
                            date,
                            startAt,
                            gameCode,
                            homeSb.getRuns(),
                            awaySb.getRuns(),
                            homeSb,
                            awaySb,
                            dto.getWinningPitcher(),
                            dto.getLosingPitcher(),
                            state
                    ));
                });

        return game;
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
