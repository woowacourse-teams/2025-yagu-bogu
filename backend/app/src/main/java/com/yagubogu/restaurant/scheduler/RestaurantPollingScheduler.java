package com.yagubogu.restaurant.scheduler;

import com.yagubogu.restaurant.service.RestaurantSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 3시에 모든 구장 주변 맛집 데이터를 한국관광공사 API로부터 동기화합니다.
 * 트래픽이 낮은 시간대를 선택하여 API 호출 영향을 최소화합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestaurantPollingScheduler {

    private final RestaurantSyncService restaurantSyncService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void pollRestaurants() {
        log.info("[RestaurantPoller] Starting daily restaurant sync");
        try {
            restaurantSyncService.syncAll();
        } catch (Exception e) {
            log.error("[RestaurantPoller] Unexpected error during restaurant sync", e);
        }
    }
}
