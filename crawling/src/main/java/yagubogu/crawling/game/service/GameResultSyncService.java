package yagubogu.crawling.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.GameCompletedEvent;
import yagubogu.crawling.game.dto.KboGameParam;
import com.yagubogu.game.dto.KboGameResultParam;
import com.yagubogu.game.repository.GameRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yagubogu.crawling.game.service.client.KboGameResultClient;
import yagubogu.crawling.game.service.client.KboGameSyncClient;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GameResultSyncService {

    private final KboGameSyncClient kboGameSyncClient;
    private final KboGameResultClient kboGameResultClient;
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

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
            log.error("Game {} has not completed yet", game.getGameCode());
            return;
        }

        if (game.getGameState().isCompleted()) {
            applicationEventPublisher.publishEvent(new GameCompletedEvent(game.getId()));
        }

        KboGameResultParam gameResult = kboGameResultClient.fetchGameResult(game);
        ScoreBoard homeScoreBoard = gameResult.homeScoreBoard();
        ScoreBoard awayScoreBoard = gameResult.awayScoreBoard();
        String homePitcher = gameResult.homePitcher();
        String awayPitcher = gameResult.awayPitcher();

        game.updateScoreBoard(homeScoreBoard, awayScoreBoard, homePitcher, awayPitcher);
    }
}
