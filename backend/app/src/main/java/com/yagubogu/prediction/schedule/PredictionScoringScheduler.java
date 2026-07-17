package com.yagubogu.prediction.schedule;

import com.yagubogu.prediction.service.PredictionScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PredictionScoringScheduler {

    private final PredictionScoringService predictionScoringService;

    @Scheduled(cron = "0 0 23 * * SUN")
    public void scoreWeeklyGames() {
        try {
            log.info("[PREDICTION] Weekly prediction scoring triggered by scheduler");
            predictionScoringService.scorePendingGames();
        } catch (RuntimeException e) {
            log.error("[{}]- {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
