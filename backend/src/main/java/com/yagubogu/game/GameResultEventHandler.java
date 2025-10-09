package com.yagubogu.game;

import com.yagubogu.game.dto.GameCompletedEvent;
import com.yagubogu.stat.service.StatService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class GameResultEventHandler {

    private final StatService statService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGameComplete(final GameCompletedEvent event) {
        int year = LocalDate.now().getYear();
        
        statService.calculateVictoryScore(year, event.gameId());
    }
}
