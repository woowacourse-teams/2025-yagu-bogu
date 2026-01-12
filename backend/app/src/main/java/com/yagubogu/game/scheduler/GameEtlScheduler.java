package com.yagubogu.game.scheduler;

import com.yagubogu.game.service.GameEtlService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Bronze → Silver ETL 스케줄러
 *
 * 설계 원칙:
 * - 크롤링: 1분마다 (실시간성)
 * - ETL: 1분마다 (실시간성)
 * - 경기 종료 시: 즉시 ETL (중요한 순간)
 * - 더블헤더 처리: ETL 후 같은 날짜 경기들의 순서 재정렬
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameEtlScheduler {

    private final GameEtlService gameEtlService;
    private final Clock clock;

    /**
     * 1분마다 Bronze → Silver ETL 실행
     *
     * 실시간 점수 제공을 위해 1분 주기 적용
     * Bronze가 1분마다 갱신되므로 ETL도 1분마다 실행
     * ETL 중에 날짜별로 더블헤더 순서가 자동 계산됨
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 10_000) // 1분 = 60,000ms
    public void runEtlPeriodically() {
        log.info("[ETL] Starting periodic Bronze → Silver transformation");

        try {
            // 최근 2분간 수집된 데이터 처리 (여유있게)
            final LocalDateTime since = LocalDateTime.now(clock).minusMinutes(2);
            final int count = gameEtlService.transformBronzeToSilver(since);

            log.info("[ETL] Completed: {} games transformed", count);
        } catch (Exception e) {
            log.error("[ETL] Failed to run periodic ETL", e);
        }
    }
}

