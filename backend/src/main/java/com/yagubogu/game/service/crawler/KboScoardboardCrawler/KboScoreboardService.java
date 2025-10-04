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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class KboScoreboardService {

    private final KboScoreboardCrawler kboScoreboardCrawler;
    private final GameRepository gameRepository;
    private final KboScoreboardMapper mapper;

    @Transactional
    public ScoreboardResponse fetchScoreboard(final LocalDate date) {
        List<KboScoreboardGame> games = kboScoreboardCrawler.crawlScoreboard(date);
        applyDoubleHeaderOrder(games);

        upsertAll(games, date);
        ;
        return new ScoreboardResponse(date, games);
    }

    private List<Game> upsertAll(List<KboScoreboardGame> list, final LocalDate date) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        List<Game> saved = new ArrayList<>(list.size());
        for (KboScoreboardGame dto : list) {
            saved.add(upsertOne(dto, date));
        }
        return saved;
    }

    private Game upsertOne(KboScoreboardGame dto, final LocalDate date) {

        // 1) 매핑
        Team awayTeam = mapper.resolveTeamFromShort(dto.getAwayTeam().name());
        Team homeTeam = mapper.resolveTeamFromShort(dto.getHomeTeam().name());
        Stadium stadium = mapper.resolveStadium(dto.getStadium());
        LocalTime startAt = parseStartAt(dto.getStartTime());

        ScoreBoard awaySb = mapper.toScoreBoard(dto.getAwayTeam());
        ScoreBoard homeSb = mapper.toScoreBoard(dto.getHomeTeam());
        GameState state = mapper.toState(dto.getStatus(), homeSb.getRuns(), awaySb.getRuns());

        int headerOrder = dto.getDoubleHeaderGameOrder();

        // 2) upsert by gameCode
        Game game = gameRepository.findByNaturalKey(date, homeTeam.getTeamCode(), awayTeam.getTeamCode(), startAt)
                .map(existing -> {
                    // 스케줄 정보 갱신(시간/구장 바뀔 수 있음)
                    existing.updateSchedule(stadium, homeTeam, awayTeam, date, startAt, state);

                    // 스코어/투수 갱신(경기 전이면 점수보드가 0일 수 있으므로 상태 보고 반영)
                    if (state != GameState.SCHEDULED) {
                        existing.updateScoreBoard(homeSb, awaySb, dto.getWinningPitcher(), dto.getLosingPitcher());
                    }
                    // 상태 최종 보정
                    existing.updateGameState(state);
                    return existing;
                })
                .orElseGet(() -> {
                    // 신규 생성
                    String gameCode = generateGameCode(date, homeTeam, awayTeam, headerOrder);
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
}
