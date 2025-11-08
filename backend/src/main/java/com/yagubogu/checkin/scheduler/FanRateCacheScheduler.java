package com.yagubogu.checkin.scheduler;

import com.yagubogu.checkin.cache.FanRateCache;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FanRateCacheScheduler {

    private final CheckInService checkInService;
    private final FanRateCache fanRateCache;

    @Scheduled(fixedRate = 1000)
    public void updateFanRateCache() {
        try {
            LocalDate today = LocalDate.now();
            List<GameWithFanRateParam> data =
                    checkInService.buildCheckInEventData(today);
            fanRateCache.put(today, data);
        } catch (Exception e) {
            log.error("Fan rate cache update failed", e);
        }
    }
}
