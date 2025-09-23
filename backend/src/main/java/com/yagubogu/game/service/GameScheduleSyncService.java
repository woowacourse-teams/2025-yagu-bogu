package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameSyncClient;
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
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameScheduleSyncService {

    private static final Map<String, String> STADIUM_NAME_MAP = Map.of(
            "문학", "랜더스필드",
            "잠실", "잠실구장",
            "사직", "사직구장",
            "광주", "챔피언스필드",
            "대전", "볼파크",
            "창원", "엔씨파크",
            "수원", "위즈파크",
            "고척", "고척돔",
            "대구", "라이온즈파크",
            "울산", "문수구장"
    );

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
            Stadium stadium = getStadiumByName(kboGameItem.stadiumName());
            Team homeTeam = getTeamByCode(kboGameItem.homeTeamCode());
            Team awayTeam = getTeamByCode(kboGameItem.awayTeamCode());
            LocalDate gameDate = kboGameItem.gameDate();
            LocalTime startAt = kboGameItem.startAt();
            String gameCode = kboGameItem.gameCode();
            GameState gameState = kboGameItem.gameState();

            Game game = new Game(
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
            games.add(game);
        }

        return games;
    }

    private Stadium getStadiumByName(final String stadiumName) {
        return stadiumRepository.findByShortName(STADIUM_NAME_MAP.get(stadiumName))
                .orElseThrow(() -> new GameSyncException("Stadium name match failed: " + stadiumName));
    }

    private Team getTeamByCode(final String teamCode) {
        return teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new GameSyncException("Team code match failed: " + teamCode));
    }
}
