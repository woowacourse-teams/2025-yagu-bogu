package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboClient;
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
@Service
public class GameSyncService {

    private static final Map<String, String> STADIUM_NAME_MAP = Map.of(
            "문학", "랜더스필드",
            "잠실", "잠실구장",
            "사직", "사직구장",
            "광주", "챔피언스필드",
            "대전", "볼파크",
            "창원", "엔씨파크",
            "수원", "위즈파크",
            "고척", "고척돔",
            "대구", "라이온즈파크"
    );

    private final KboClient kboClient;
    private final GameRepository gameRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;

    public void syncGameSchedule(final LocalDate date) {
        KboGamesResponse kboGamesResponse = kboClient.fetchGames(date);
        List<Game> games = convertToGames(kboGamesResponse);

        gameRepository.saveAll(games);
    }

    public List<Game> convertToGames(final KboGamesResponse kboGamesResponse) {
        List<Game> games = new ArrayList<>();

        for (KboGameResponse kboGameItem : kboGamesResponse.games()) {
            Stadium stadium = getStadiumByName(kboGameItem.stadiumName());
            Team homeTeam = getTeamByShortName(kboGameItem.homeTeamName());
            Team awayTeam = getTeamByShortName(kboGameItem.awayTeamName());
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
                    gameState
            );
            games.add(game);
        }

        return games;
    }

    @Transactional
    public void syncGameResult(LocalDate date) {
        List<KboGameResponse> gameResponses = kboClient.fetchGames(date).games();

        for (KboGameResponse response : gameResponses) {
            gameRepository.findByGameCode(response.gameCode())
                    .ifPresent(game -> updateGameDetails(game, response));
        }
    }

    private void updateGameDetails(Game game, KboGameResponse response) {
        game.updateGameState(response.gameState());

        if (game.getGameState().isNotCompleted()) {
            // TODO: gameState가 LIVE 일 때 로깅
            return;
        }

        KboGameResultResponse gameResult = kboClient.fetchGameResult(game);
        ScoreBoard homeScoreBoard = gameResult.homeScoreBoard().toScoreBoard();
        ScoreBoard awayScoreBoard = gameResult.awayScoreBoard().toScoreBoard();

        game.updateScoreBoard(homeScoreBoard, awayScoreBoard);
    }


    private Stadium getStadiumByName(final String stadiumName) {
        return stadiumRepository.findByShortName(STADIUM_NAME_MAP.get(stadiumName))
                .orElseThrow(() -> new GameSyncException("Stadium name match failed: " + stadiumName));
    }

    private Team getTeamByShortName(final String teamShortName) {
        return teamRepository.findByShortName(teamShortName)
                .orElseThrow(() -> new GameSyncException("Team code match failed: " + teamShortName));
    }
}
