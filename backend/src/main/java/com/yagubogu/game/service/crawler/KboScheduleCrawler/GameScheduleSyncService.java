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
        KboGamesResponse kboGamesResponse = kboGameSyncClient.fetchGames(date);
        List<Game> games = convertToGames(kboGamesResponse);

        gameRepository.saveAll(games);
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
}
