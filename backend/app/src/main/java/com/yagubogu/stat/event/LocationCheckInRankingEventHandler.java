package com.yagubogu.stat.event;

import com.yagubogu.checkin.dto.event.LocationCheckInCreatedEvent;
import com.yagubogu.stat.repository.LocationCheckInRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class LocationCheckInRankingEventHandler {

    private final LocationCheckInRankingRepository locationCheckInRankingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLocationCheckInCreated(final LocationCheckInCreatedEvent event) {
        try {
            locationCheckInRankingRepository.upsertIncrement(event.memberId(), event.gameYear());
        } catch (RuntimeException e) {
            log.error("[STAT] Failed to update location check-in ranking: memberId={}, gameYear={}",
                    event.memberId(), event.gameYear(), e);
        }
    }
}
