package com.yagubogu.stat.event;

import com.yagubogu.checkin.dto.event.LocationCheckInCreatedEvent;
import com.yagubogu.stat.repository.LocationCheckInRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class LocationCheckInRankingEventHandler {

    private final LocationCheckInRankingRepository locationCheckInRankingRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleLocationCheckInCreated(final LocationCheckInCreatedEvent event) {
        locationCheckInRankingRepository.upsertIncrement(event.memberId(), event.gameYear());
    }
}
