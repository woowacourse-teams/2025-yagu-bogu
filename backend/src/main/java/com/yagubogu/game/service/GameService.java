package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.KboClientResponse;
import com.yagubogu.game.dto.KboGameItemDto;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboClient;
import com.yagubogu.global.exception.KboClientException;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GameService {

    private final KboClient kboClient;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;


    public void fetchGameList(final LocalDate date) {
        KboClientResponse kboClientResponse = kboClient.fetchGame(date);
        List<Game> games = new ArrayList<>();

        for (KboGameItemDto kboGameItem : kboClientResponse.games()) {
            Stadium stadium = getStadiumByName(kboGameItem.stadiumName());
            Team homeTeam = getTeamByShortName(kboGameItem.homeTeamName());
            Team awayTeam = getTeamByShortName(kboGameItem.awayTeamName());
            LocalDate gameDate = LocalDate.parse(kboGameItem.gameDate());
            LocalTime startAt = LocalTime.parse(kboGameItem.startAt());
            String gameCode = kboGameItem.gameCode();

            Game game = new Game(stadium, homeTeam, awayTeam, gameDate, startAt, gameCode, null, null);

            games.add(game);
        }
        gameRepository.saveAll(games);
    }

    private Stadium getStadiumByName(final String stadiumName) {
        return stadiumRepository.findByShortName(STADIUM_NAME_MAP.get(stadiumName))
                .orElseThrow(() -> new KboClientException("Stadium name match failed: " + stadiumName));
    }

    private Team getTeamByShortName(final String teamShortName) {
        return teamRepository.findByShortName(teamShortName)
                .orElseThrow(() -> new KboClientException("Team code match failed: " + teamShortName));
    }
}
