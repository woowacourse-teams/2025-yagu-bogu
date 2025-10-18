package yagubogu.crawling.game.service.crawler.KboScheduleCrawler;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
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
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yagubogu.crawling.game.dto.KboGameParam;
import yagubogu.crawling.game.dto.KboGamesParam;
import yagubogu.crawling.game.service.client.KboGameSyncClient;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameScheduleSyncService {

    private final KboGameSyncClient kboGameSyncClient;
    private final KboSchedulerCrawler kboSchedulerCrawler;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Transactional
    public void syncGameSchedule(final LocalDate date) {
        yagubogu.crawling.game.dto.KboGamesParam KboGamesParam = kboGameSyncClient.fetchGames(date);
        List<Game> games = convertToGames(KboGamesParam);

        gameRepository.saveAll(games);
    }

    /**
     * 지정한 기간(start~end)에 대해 크롤러로 수집 후 upsert 수행합니다.
     * - 기존 존재 시 업데이트, 없으면 생성
     */
    @Transactional
    public void syncByCrawler(final LocalDateTime now, final LocalDate startDate, final LocalDate endDate,
                              final ScheduleType scheduleType) {
        List<KboGame> games = kboSchedulerCrawler.crawlKboSchedule(startDate, endDate, scheduleType);
        upsertByCrawlerGames(now, games);
    }

    private void upsertByCrawlerGames(final LocalDateTime now, final List<KboGame> games) {
        for (KboGame kboGame : games) {
            String homeTeamName = kboGame.getHomeTeam();
            Optional<Team> homeTeamOpt = teamRepository.findByShortName(homeTeamName);
            String awayTeamName = kboGame.getAwayTeam();
            Optional<Team> awayTeamOpt = teamRepository.findByShortName(awayTeamName);
            if (homeTeamOpt.isEmpty()) {
                log.error("팀이 존재하지 않습니다 : homeTeam {}", homeTeamName);
                continue;
            }
            if (awayTeamOpt.isEmpty()) {
                log.error("팀이 존재하지 않습니다 : awayTeam {}", awayTeamName);
                continue;
            }

            Team homeTeam = homeTeamOpt.get();
            Team awayTeam = awayTeamOpt.get();

            Stadium stadium = getStadiumByLocation(kboGame.getStadium());
            LocalDate date = kboGame.getDate();
            LocalTime startAt = parseGameTime(kboGame.getGameTime());
            int headerOrder = kboGame.getDoubleHeaderGameOrder();
            Integer homeScore = kboGame.getHomeScore();
            Integer awayScore = kboGame.getAwayScore();
            String gameCode = generateGameCode(date, homeTeam, awayTeam, headerOrder);

            Optional<Game> existingOpt = gameRepository.findByGameCode(gameCode);
            if (existingOpt.isPresent()) {
                Game existing = existingOpt.get();
                GameState state = getGameState(now, kboGame);
                existing.updateSchedule(stadium, homeTeam, awayTeam, date, startAt, state);
                return;
            }

            GameState state = getGameState(now, kboGame);
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

    private GameState getGameState(final LocalDateTime now, final KboGame k) {
        LocalTime time = LocalTime.parse(k.getGameTime());
        LocalDateTime gameTime = LocalDateTime.of(k.getDate(), time);
        // 해당 사이트에서는 완료 여부 확인 불가
        if (gameTime.isBefore(now)) {
            return GameState.COMPLETED;
        }
        return GameState.SCHEDULED;
    }

    private List<Game> convertToGames(final KboGamesParam KboGamesParam) {
        List<Game> games = new ArrayList<>();

        for (KboGameParam kboGameItem : KboGamesParam.games()) {
            games.add(buildGameFrom(kboGameItem));
        }

        return games;
    }

    private Game buildGameFrom(final KboGameParam kboGameItem) {
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
        final String yyyymmdd = date.format(BASIC_ISO_DATE);

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
