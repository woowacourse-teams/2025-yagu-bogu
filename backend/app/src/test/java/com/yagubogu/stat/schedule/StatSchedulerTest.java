package com.yagubogu.stat.schedule;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.yagubogu.stat.service.LocationCheckInRankingSyncService;
import com.yagubogu.stat.service.StatSyncService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatSchedulerTest {

    private final StatSyncService statSyncService = mock(StatSyncService.class);
    private final LocationCheckInRankingSyncService locationCheckInRankingSyncService =
            mock(LocationCheckInRankingSyncService.class);
    private final StatScheduler statScheduler =
            new StatScheduler(statSyncService, locationCheckInRankingSyncService);

    @DisplayName("위치 기반 직관 랭킹을 재집계한다")
    @Test
    void syncLocationCheckInRanking() {
        // when
        statScheduler.syncLocationCheckInRanking();

        // then
        verify(locationCheckInRankingSyncService).rebuildAll();
    }

    @DisplayName("위치 기반 직관 랭킹 재집계에 실패해도 예외를 전파하지 않는다")
    @Test
    void syncLocationCheckInRanking_fail() {
        // given
        doThrow(new RuntimeException("sync failed"))
                .when(locationCheckInRankingSyncService)
                .rebuildAll();

        // when & then
        assertThatCode(statScheduler::syncLocationCheckInRanking)
                .doesNotThrowAnyException();
    }
}
