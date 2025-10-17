package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboGameParam;
import com.yagubogu.game.dto.KboGameResultParam;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameResultClient;
import com.yagubogu.game.service.client.KboGameSyncClient;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameResultSyncService {

    private final KboGameSyncClient kboGameSyncClient;
    private final KboGameResultClient kboGameResultClient;
    private final GameRepository gameRepository;

    @Transactional
    public void syncGameResult(LocalDate date) {
        List<KboGameParam> gameResponses = kboGameSyncClient.fetchGames(date).games();

        for (KboGameParam response : gameResponses) {
            gameRepository.findByGameCode(response.gameCode())
                    .ifPresent(game -> updateGameDetails(game, response));
        }
    }

    private void updateGameDetails(Game game, KboGameParam response) {
        game.updateGameState(response.gameState());

        if (game.getGameState().isNotCompleted()) {
            // TODO: gameState가 LIVE 일 때 로깅
            return;
        }

        KboGameResultParam gameResult = kboGameResultClient.fetchGameResult(game);
        ScoreBoard homeScoreBoard = gameResult.homeScoreBoard();
        ScoreBoard awayScoreBoard = gameResult.awayScoreBoard();
        String homePitcher = gameResult.homePitcher();
        String awayPitcher = gameResult.awayPitcher();

        game.updateScoreBoard(homeScoreBoard, awayScoreBoard, homePitcher, awayPitcher);
    }
}
