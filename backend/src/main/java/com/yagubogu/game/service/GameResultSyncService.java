package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGameResultResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.client.KboGameResultClient;
import com.yagubogu.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameResultSyncService {

    private final KboGameResultClient kboGameResultClient;
    private final GameRepository gameRepository;

    @Transactional
    public void updateGameDetails(String gameCode, KboGameResponse response) {
        Game game = findGameByGameCode(gameCode);
        game.updateGameState(response.gameState());

        if (response.gameState() == GameState.SCHEDULED || response.gameState() == GameState.CANCELED) {
            return;
        }

        KboGameResultResponse gameResult = kboGameResultClient.fetchGameResult(game);

        ScoreBoard homeScoreBoard = gameResult.homeScoreBoard();
        ScoreBoard awayScoreBoard = gameResult.awayScoreBoard();
        String homePitcher = gameResult.homePitcher();
        String awayPitcher = gameResult.awayPitcher();
        game.updateScoreBoard(homeScoreBoard, awayScoreBoard, homePitcher, awayPitcher);
    }

    private Game findGameByGameCode(final String gameCode) {
        return gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameCode));
    }
}
