package com.yagubogu.place.service;

import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.PlaceSyncResult;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 전체 구장 x 카테고리 조합을 순회하는 오케스트레이터.
 * 실제 동기화(트랜잭션 단위)는 {@link PlaceStadiumSyncService}에 위임한다 —
 * 이 클래스 안에서 직접 처리하면 self-invocation이 되어 {@code @Transactional}이 무시된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaceSyncService {

    private final PlaceStadiumSyncService placeStadiumSyncService;
    private final StadiumRepository stadiumRepository;

    /**
     * 스케줄러와 관리자 수동 트리거가 같은 진입점을 쓰므로 동시 실행을 막는다.
     * 한 번의 전체 동기화는 목록 조회 외에 신규 장소마다 상세 조회가 2회씩 붙어 호출량이 크고,
     * 공공데이터포털 서비스키에는 일일 트래픽 한도가 있어 중복 실행이 그대로 한도 소모로 이어진다.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * @return 동기화 집계. 이미 다른 동기화가 진행 중이면 {@link Optional#empty()}
     */
    public Optional<PlaceSyncResult> syncAll() {
        if (!running.compareAndSet(false, true)) {
            log.warn("[PlaceSync] Sync already in progress; skipping this trigger");
            return Optional.empty();
        }

        try {
            return Optional.of(doSyncAll());
        } finally {
            running.set(false);
        }
    }

    private PlaceSyncResult doSyncAll() {
        List<Stadium> stadiums = stadiumRepository.findAll();
        log.info("[PlaceSync] Starting sync for {} stadiums x {} categories",
                stadiums.size(), PlaceCategory.values().length);

        int success = 0;
        int failed = 0;

        for (Stadium stadium : stadiums) {
            for (PlaceCategory category : PlaceCategory.values()) {
                try {
                    placeStadiumSyncService.syncForStadium(stadium, category);
                    success++;
                } catch (Exception e) {
                    log.error("[PlaceSync] Failed to sync stadiumId={} ({}) category={}: {}",
                            stadium.getId(), stadium.getShortName(), category, e.getMessage());
                    failed++;
                }
            }
        }

        log.info("[PlaceSync] Completed: success={}, failed={}", success, failed);

        return new PlaceSyncResult(success, failed);
    }
}
