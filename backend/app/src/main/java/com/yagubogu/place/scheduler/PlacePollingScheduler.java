package com.yagubogu.place.scheduler;

import com.yagubogu.place.service.PlaceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 3시에 모든 구장 주변 장소(맛집/카페/숙소/관광지/공연) 데이터를 한국관광공사 API로부터 동기화합니다.
 * 트래픽이 낮은 시간대를 선택하여 API 호출 영향을 최소화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlacePollingScheduler {

    private final PlaceSyncService placeSyncService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void pollPlaces() {
        log.info("[PlacePoller] Starting daily place sync");
        try {
            placeSyncService.syncAll();
        } catch (Exception e) {
            log.error("[PlacePoller] Unexpected error during place sync", e);
        }
    }
}
