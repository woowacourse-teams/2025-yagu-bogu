package com.yagubogu.stat.event;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.yagubogu.checkin.dto.event.LocationCheckInCreatedEvent;
import com.yagubogu.stat.repository.LocationCheckInRankingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LocationCheckInRankingEventHandlerTest {

    private final LocationCheckInRankingRepository locationCheckInRankingRepository =
            mock(LocationCheckInRankingRepository.class);
    private final LocationCheckInRankingEventHandler handler =
            new LocationCheckInRankingEventHandler(locationCheckInRankingRepository);

    @DisplayName("위치 기반 체크인 생성 이벤트를 받으면 직관 랭킹 카운트를 증가시킨다")
    @Test
    void handleLocationCheckInCreated() {
        // given
        long memberId = 1L;
        int gameYear = 2025;
        LocationCheckInCreatedEvent event = new LocationCheckInCreatedEvent(memberId, gameYear);

        // when
        handler.handleLocationCheckInCreated(event);

        // then
        verify(locationCheckInRankingRepository).upsertIncrement(memberId, gameYear);
    }
}
