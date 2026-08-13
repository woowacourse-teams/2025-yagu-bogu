package com.yagubogu.place.service;

import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import java.util.List;
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

    public void syncAll() {
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
    }
}
