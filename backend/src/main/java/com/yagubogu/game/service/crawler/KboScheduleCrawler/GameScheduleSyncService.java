package com.yagubogu.game.service.crawler.KboScheduleCrawler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameSyncClient;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameScheduleSyncService {

    private final KboGameSyncClient kboGameSyncClient;
    private final KboScheduleCrawler kboScheduleCrawler;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Transactional
    public void syncGameSchedule(final LocalDate date) {
        KboGamesResponse kboGamesResponse = kboGameSyncClient.fetchGames(date);
        List<Game> games = convertToGames(kboGamesResponse);

        gameRepository.saveAll(games);
    }

    /**
     * 지정한 기간(start~end)에 대해 크롤러로 수집 후 upsert 수행합니다.
     * - 기존 존재 시 업데이트, 없으면 생성
     * - 반환값은 크롤링으로 수집한 경기 수입니다.
     */
    @Transactional
    public int syncByCrawler(final LocalDate now, final LocalDate startDate, final LocalDate endDate,
                             final ScheduleType scheduleType) {
        List<KboGame> games = kboScheduleCrawler.crawlKboSchedule(startDate, endDate, scheduleType);
        upsertByCrawlerGames(now, games, true, true);
        return games.size();
    }

    private void upsertByCrawlerGames(final LocalDate now,
                                      final List<KboGame> games,
                                      final boolean updateExisting,
                                      final boolean insertIfMissing
    ) {
        for (KboGame kboGame : games) {
            Optional<Team> homeTeamOpt = teamRepository.findByShortName(kboGame.getHomeTeam());
            Optional<Team> awayTeamOpt = teamRepository.findByShortName(kboGame.getAwayTeam());
            if (homeTeamOpt.isEmpty() || awayTeamOpt.isEmpty()) {
                continue;
            }

            Team homeTeam = homeTeamOpt.get();
            Team awayTeam = awayTeamOpt.get();

            Stadium stadium = getStadiumByLocation(kboGame.getStadium());
            LocalDate date = kboGame.getDate();
            LocalTime startAt = parseGameTime(kboGame.getGameTime());
            int headerOrder = Math.max(0, kboGame.getDoubleHeaderGameOrder());
            Integer homeScore = kboGame.getHomeScore();
            Integer awayScore = kboGame.getAwayScore();
            String gameCode = generateGameCode(date, homeTeam, awayTeam, headerOrder);

            Optional<Game> existingOpt = gameRepository.findByGameCode(gameCode)
                    .or(() -> gameRepository.findByDateAndHomeTeamAndAwayTeamAndStartAt(date, homeTeam, awayTeam,
                            startAt));
            if (existingOpt.isPresent()) {
                if (updateExisting) {
                    Game existing = existingOpt.get();
                    GameState state = getGameState(now, kboGame, date);
                    existing.updateSchedule(stadium, homeTeam, awayTeam, date, startAt, state);
                }
                return;
            }

            if (insertIfMissing) {
                GameState state = getGameState(now, kboGame, date);
                Game game = new Game(
                        stadium,
                        homeTeam,
                        awayTeam,
                        date,
                        startAt,
                        gameCode,
                        homeScore,
                        awayScore,
                        null,
                        null,
                        null,
                        null,
                        state
                );
                gameRepository.save(game);
            }
        }
    }

    private GameState getGameState(final LocalDate now, final KboGame k, final LocalDate date) {
        if (k.isCancelled()) {
            return GameState.CANCELED;
        }
        if (date.isBefore(now)) {
            return GameState.COMPLETED;
        }
        return GameState.SCHEDULED;
    }

    private List<Game> convertToGames(final KboGamesResponse kboGamesResponse) {
        List<Game> games = new ArrayList<>();

        for (KboGameResponse kboGameItem : kboGamesResponse.games()) {
            games.add(buildGameFrom(kboGameItem));
        }

        return games;
    }

    private Game buildGameFrom(final KboGameResponse kboGameItem) {
        Stadium stadium = getStadiumByLocation(kboGameItem.stadiumName());
        Team homeTeam = getTeamByCode(kboGameItem.homeTeamCode());
        Team awayTeam = getTeamByCode(kboGameItem.awayTeamCode());
        LocalDate gameDate = kboGameItem.gameDate();
        LocalTime startAt = kboGameItem.startAt();
        String gameCode = kboGameItem.gameCode();
        GameState gameState = kboGameItem.gameState();

        return new Game(
                stadium,
                homeTeam,
                awayTeam,
                gameDate,
                startAt,
                gameCode,
                null,
                null,
                null,
                null,
                null,
                null,
                gameState
        );
    }

    private Stadium getStadiumByLocation(final String location) {
        return stadiumRepository.findByLocation(location)
                .orElseThrow(() -> new NotFoundException("Stadium name match failed: " + location));
    }

    private Team getTeamByCode(final String teamCode) {
        return teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new NotFoundException("Team code match failed: " + teamCode));
    }

    private String generateGameCode(final LocalDate date, final Team homeTeam, final Team awayTeam,
                                    final int headerOrder) {
        final String yyyymmdd = date.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);

        return yyyymmdd + awayTeam.getTeamCode() + homeTeam.getTeamCode() + headerOrder;
    }

    private LocalTime parseGameTime(final String raw) {
        if (raw == null || raw.isBlank()) {
            return LocalTime.MIDNIGHT;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d{1,2}):(\\d{2})").matcher(raw);
        if (m.find()) {
            try {
                int hh = Integer.parseInt(m.group(1));
                int mm = Integer.parseInt(m.group(2));
                return LocalTime.of(Math.min(Math.max(hh, 0), 23), Math.min(Math.max(mm, 0), 59));
            } catch (Exception ignored) {
                return LocalTime.MIDNIGHT;
            }
        }
        return LocalTime.MIDNIGHT;
    }
}
