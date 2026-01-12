package com.yagubogu.game.service.crawler.KboScheduleCrawler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.KboGameParam;
import com.yagubogu.game.dto.KboGamesParam;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameSyncClient;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameScheduleSyncService {

    private final KboGameSyncClient kboGameSyncClient;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    @Transactional
    public void syncGameSchedule(final LocalDate date) {
        KboGamesParam kboGamesParam = kboGameSyncClient.fetchGames(date);
        List<Game> games = convertToGames(kboGamesParam);

        gameRepository.saveAll(games);
    }

    private List<Game> convertToGames(final KboGamesParam kboGamesParam) {
        List<Game> games = new ArrayList<>();

        for (KboGameParam kboGameItem : kboGamesParam.games()) {
            buildGameFrom(kboGameItem).ifPresent(games::add);
        }

        return games;
    }

    private Optional<Game> buildGameFrom(final KboGameParam item) {
        Optional<Stadium> stadiumOpt = findStadium(item);
        if (stadiumOpt.isEmpty()) {
            return Optional.empty();
        }

        Optional<Team> homeTeamOpt = findTeam(item.homeTeamCode(), "HOME", item);
        Optional<Team> awayTeamOpt = findTeam(item.awayTeamCode(), "AWAY", item);
        if (homeTeamOpt.isEmpty() || awayTeamOpt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new Game(
                stadiumOpt.get(),
                homeTeamOpt.get(),
                awayTeamOpt.get(),
                item.gameDate(),
                item.startAt(),
                item.gameCode(),
                null,
                null,
                null,
                null,
                null,
                null,
                item.gameState()
        ));
    }

    private Optional<Stadium> findStadium(final KboGameParam item) {
        Optional<Stadium> stadiumOpt = stadiumRepository.findByLocation(item.stadiumName());

        if (stadiumOpt.isEmpty()) {
            log.warn(
                    "Game schedule skipped - stadium mapping failed: gameCode={}, stadiumName={}",
                    item.gameCode(),
                    item.stadiumName()
            );
        }

        return stadiumOpt;
    }

    private Optional<Team> findTeam(
            final String teamCode,
            final String side,
            final KboGameParam item
    ) {
        Optional<Team> teamOpt = teamRepository.findByTeamCode(teamCode);

        if (teamOpt.isEmpty()) {
            log.warn(
                    "Game schedule skipped - team mapping failed: gameCode={}, side={}, teamCode={}",
                    item.gameCode(),
                    side,
                    teamCode
            );
        }

        return teamOpt;
    }
}
