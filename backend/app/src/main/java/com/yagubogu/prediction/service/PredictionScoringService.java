package com.yagubogu.prediction.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.dto.WeeklyScoreParam;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PredictionScoringService {

    private static final List<GameState> FINALIZED_GAME_STATES = List.of(GameState.COMPLETED, GameState.CANCELED);

    private final GamePredictionRepository gamePredictionRepository;

    @Transactional
    public void scorePendingGames() {
        List<Game> pendingGames = gamePredictionRepository.findGamesByPredictionStatusAndGameStateIn(
                PredictionStatus.SUBMITTED, FINALIZED_GAME_STATES
        );

        for (Game game : pendingGames) {
            scoreGame(game);
        }
    }

    public List<WeeklyScoreParam> findWeeklyScores(final LocalDate weekStart, final LocalDate weekEnd) {
        return gamePredictionRepository.findWeeklyScores(PredictionStatus.WON, weekStart, weekEnd);
    }

    private void scoreGame(final Game game) {
        List<GamePrediction> predictions = gamePredictionRepository.findAllByGame(game);
        for (GamePrediction prediction : predictions) {
            prediction.applyResult(game);
        }
    }
}
