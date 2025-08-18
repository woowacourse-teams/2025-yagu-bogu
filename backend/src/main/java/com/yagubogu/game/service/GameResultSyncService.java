package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoardSummary;
import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGameSummaryResultResponse;
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
        List<KboGameResponse> gameResponses = kboGameSyncClient.fetchGames(date).games();

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

        KboGameSummaryResultResponse gameResult = kboGameResultClient.fetchGameResult(game);
        ScoreBoardSummary homeScoreBoardSummary = gameResult.homeScoreBoard().toScoreBoard();
        ScoreBoardSummary awayScoreBoardSummary = gameResult.awayScoreBoard().toScoreBoard();

        game.updateScoreBoard(homeScoreBoardSummary, awayScoreBoardSummary);
    }
}
