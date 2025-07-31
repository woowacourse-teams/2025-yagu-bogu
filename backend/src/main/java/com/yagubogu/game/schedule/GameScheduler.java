package com.yagubogu.game.schedule;

import com.yagubogu.game.service.GameService;
import com.yagubogu.global.exception.KboClientException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final GameService gameService;

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGame() {
        LocalDate today = LocalDate.now();

        try {
            gameService.fetchGameList(today);
        } catch (KboClientException e) {
            //TODO: 예외 로깅
        }
    }
}
